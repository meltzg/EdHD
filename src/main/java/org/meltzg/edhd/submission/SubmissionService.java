package org.meltzg.edhd.submission;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.hadoop.IHadoopService;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.meltzg.edhd.storage.AbstractStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SubmissionService extends AbstractSubmissionService {

    private static final String ASSIGNMENTID = "assignmentId";
    private static final String USER = "name";
    private static final String CONFIGLOC = "configLoc";
    private static final String SRCLOC = "srcLoc";
    private static final String ISVALIDATION = "isValidation";

    // Status Info
    private static final String COMPILESTATUS = "compileStatus";
    private static final String COMPILEMSG = "compileMsg";
    private static final String RUNSTATUS = "runStatus";
    private static final String RUNMSG = "runMsg";
    private static final String VALIDATESTATUS = "validateStatus";
    private static final String VALIDATEMSG = "validateMsg";
    private static final String COMPLETESTATUS = "completeStatus";
    private static final String COMPLETEMSG = "completeMsg";
    @Autowired
    private IHadoopService hadoopService;
    @Value("${edhd.threads}")
    private Integer nThreads;
    @Autowired
    private AbstractStorageService storageService;
    @Autowired
    private AbstractSecurityService securityService;
    private ExecutorService threadpool;

    @PostConstruct
    public void init() throws Exception {
        super.init();
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + ID + " UUID, " + ASSIGNMENTID
                + " UUID, " + USER + " TEXT REFERENCES " + securityService.TABLE_NAME() + ", " + CONFIGLOC
                + " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + SRCLOC + " UUID REFERENCES "
                + storageService.TABLE_NAME() + ", " + ISVALIDATION + " BOOLEAN, " + COMPILESTATUS + " INTEGER, "
                + RUNSTATUS + " INTEGER, " + VALIDATESTATUS + " INTEGER, " + COMPLETESTATUS + " INTEGER, " + COMPILEMSG
                + " TEXT, " + RUNMSG + " TEXT, " + VALIDATEMSG + " TEXT, " + COMPLETEMSG + " TEXT, PRIMARY KEY(" + ID
                + "))";
        statement.executeUpdate(createUsers);
        conn.close();

        threadpool = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public String TABLE_NAME() {
        return "submissions";
    }

    @Override
    public UUID executeDefinition(AssignmentDefinition definition)
            throws IOException, ClassNotFoundException, SQLException {
        return executeSubmission(definition, true, false);
    }

    @Override
    public UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission, MultipartFile src)
            throws IOException, ClassNotFoundException, SQLException {
        UUID srcLoc = storageService.putFile(src);
        UUID configLoc = storageService.putFile(new GenJobConfiguration(submission.getConfig()));

        definition.setConfig(submission.getConfig());
        definition.setConfigLoc(configLoc);
        definition.setSrcName(submission.getSrcName());
        definition.setSrcLoc(srcLoc);
        definition.setUser(submission.getUser());
        return executeSubmission(definition, false, true);
    }

    @Override
    public StatusProperties getStatus(UUID id, boolean isAdmin, String user)
            throws ClassNotFoundException, SQLException {
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(id, DBType.UUID));
        ResultSet rs = executeQuery(
                "SELECT * FROM " + TABLE_NAME() + " WHERE " + ID + "=? ORDER BY " + ISVALIDATION + " DESC;", params);
        if (rs.next()) {
            String statUser = rs.getString(USER);
            UUID assignmentId = (UUID) rs.getObject(ASSIGNMENTID);
            boolean isValidation = rs.getBoolean(ISVALIDATION);
            StatusValue compileStatus = StatusValue.fromInteger(rs.getInt(COMPILESTATUS));
            String compileMsg = rs.getString(COMPILEMSG);
            StatusValue runStatus = StatusValue.fromInteger(rs.getInt(RUNSTATUS));
            String runMsg = rs.getString(RUNMSG);
            StatusValue validateStatus = StatusValue.fromInteger(rs.getInt(VALIDATESTATUS));
            String validateMsg = rs.getString(VALIDATEMSG);
            StatusValue completeStatus = StatusValue.fromInteger(rs.getInt(COMPLETESTATUS));
            String completeMsg = rs.getString(COMPLETEMSG);

            StatusProperties props = new StatusProperties(id, statUser, assignmentId, isValidation, compileStatus,
                    compileMsg, runStatus, runMsg, validateStatus, validateMsg, completeStatus, completeMsg);
            if (isAdmin || props.isValidation() || props.getUser().equals(user)) {
                return props;
            }
        }

        return null;
    }

    @Override
    public List<UUID> getSubmissionIds(UUID assignmentId, String user) throws ClassNotFoundException, SQLException {
        List<UUID> ids = new ArrayList<UUID>();
        List<StatementParameter> params = new ArrayList<StatementParameter>();

        boolean isAdmin = securityService.isAdmin(user);

        String query = "SELECT " + ID + " FROM " + TABLE_NAME() + " WHERE " + ASSIGNMENTID + "=?";
        params.add(new StatementParameter(assignmentId, DBType.UUID));
        if (!isAdmin) {
            query += " AND (" + USER + "=? OR " + ISVALIDATION + "=?)";
            params.add(new StatementParameter(user, DBType.TEXT));
            params.add(new StatementParameter(true, DBType.BOOLEAN));
        }
        query += ";";

        ResultSet rs = executeQuery(query, params);
        while (rs.next()) {
            UUID id = (UUID) rs.getObject(ID);
            ids.add(id);
        }

        return ids;
    }

    @Override
    public void updateStatus(StatusProperties status) throws ClassNotFoundException, SQLException {
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(status.getCompileStatus().value(), DBType.INT));
        params.add(new StatementParameter(status.getCompileMsg(), DBType.TEXT));
        params.add(new StatementParameter(status.getRunStatus().value(), DBType.INT));
        params.add(new StatementParameter(status.getRunMsg(), DBType.TEXT));
        params.add(new StatementParameter(status.getValidateStatus().value(), DBType.INT));
        params.add(new StatementParameter(status.getValidateMsg(), DBType.TEXT));
        params.add(new StatementParameter(status.getCompleteStatus().value(), DBType.INT));
        params.add(new StatementParameter(status.getCompleteMsg(), DBType.TEXT));
        params.add(new StatementParameter(status.getId(), DBType.UUID));

        executeUpdate("UPDATE " + TABLE_NAME() + " SET " + COMPILESTATUS + "=?, " + COMPILEMSG + "=?, " + RUNSTATUS
                + "=?, " + RUNMSG + "=?, " + VALIDATESTATUS + "=?, " + VALIDATEMSG + "=?, " + COMPLETESTATUS + "=?, "
                + COMPLETEMSG + "=? WHERE " + ID + "=?;", params);
    }

    @Override
    public void deleteByAssignment(UUID id) {
        List<AssignmentSubmission> submissionsToDelete = getByAssignment(id);
        for (AssignmentSubmission sub : submissionsToDelete) {
            try {
                deleteById(sub.getId(), true);
            } catch (ClassNotFoundException | SQLException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean deleteByUserAssignment(String user, UUID assignmentId, boolean isValidation, boolean deleteOldFiles)
            throws ClassNotFoundException, SQLException, IOException {
        AssignmentSubmission existingSubmission = getByUserAssignment(user, assignmentId, isValidation);
        if (existingSubmission != null) {
            UUID srcLoc = existingSubmission.getSrcLoc();
            UUID configLoc = existingSubmission.getConfigLoc();
            deleteById(TABLE_NAME(), existingSubmission.getId());

            if (deleteOldFiles) {
                storageService.deleteFile(configLoc);
                storageService.deleteFile(srcLoc);
            }
            hadoopService.delete(SubmissionWorker.SUBMISSION_DIR + existingSubmission.getId());
            hadoopService.delete(SubmissionWorker.VALIDATION_DIR + existingSubmission.getId());

            return true;
        }

        return false;
    }

    @Override
    public StatusProperties getValidatorStatProps(UUID assignmentId) {
        AssignmentSubmission submission = getByUserAssignment(null, assignmentId, true);
        if (submission != null) {
            try {
                StatusProperties stat = getStatus(submission.getId(), true, null);
                return stat;
            } catch (ClassNotFoundException | SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean validatorPending(UUID assignmentId) {
        StatusProperties stat = getValidatorStatProps(assignmentId);
        if (stat != null) {
            return stat.getCompileStatus().equals(StatusValue.PENDING);
        }
        return false;
    }

    @Override
    public boolean submissionPending(UUID assignmentId, String user) {
        AssignmentSubmission submission = getByUserAssignment(user, assignmentId, false);
        if (submission != null) {
            try {
                StatusProperties stat = getStatus(submission.getId(), false, user);
                return stat.getCompileStatus().equals(StatusValue.PENDING);
            } catch (ClassNotFoundException | SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public AssignmentSubmission getByUserAssignment(String user, UUID assignmentId, boolean isValidation) {
        AssignmentSubmission submission = null;
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(assignmentId, DBType.UUID));
        params.add(new StatementParameter(isValidation, DBType.BOOLEAN));

        String queryString = "SELECT * FROM " + TABLE_NAME() + " WHERE " + ASSIGNMENTID + "=? AND " + ISVALIDATION
                + "=?";
        if (!isValidation) {
            queryString += " AND " + USER + "=?";
            params.add(new StatementParameter(user, DBType.TEXT));
        } else {
            queryString += " AND " + USER + " IS NULL";
        }
        queryString += ";";

        try {
            ResultSet rs = executeQuery(queryString, params);
            if (rs.next()) {
                submission = extractSubmissionProps(rs);
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return submission;
    }

    @Override
    public void revalidateSubmissions(AssignmentDefinition definition) {
        List<AssignmentSubmission> submissions = getByAssignment(definition.getId());
        for (AssignmentSubmission submission : submissions) {
            try {
                StatusProperties stat = getStatus(submission.getId(), true, null);
                if (stat != null && !stat.isValidation()) {
                    AssignmentDefinition tmpDef = new AssignmentDefinition(definition);
                    tmpDef.setConfig(submission.getConfig());
                    tmpDef.setConfigLoc(submission.getConfigLoc());
                    tmpDef.setSrcName(submission.getSrcName());
                    tmpDef.setSrcLoc(submission.getSrcLoc());
                    tmpDef.setUser(submission.getUser());
                    stat.resetStatus();
                    updateStatus(stat);
                    executeSubmission(tmpDef, false, false);
                }
            } catch (ClassNotFoundException | SQLException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private UUID executeSubmission(AssignmentDefinition definition, boolean isValidation, boolean deleteOldFiles)
            throws IOException, ClassNotFoundException, SQLException {
        UUID submissionId = UUID.randomUUID();
        StatusProperties statProps = new StatusProperties(submissionId);
        statProps.setValidation(isValidation);

        deleteByUserAssignment(definition.getUser(), definition.getId(), isValidation, deleteOldFiles);

        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(submissionId, DBType.UUID));
        params.add(new StatementParameter(definition.getUser(), DBType.TEXT));
        params.add(new StatementParameter(definition.getId(), DBType.UUID));
        params.add(new StatementParameter(definition.getConfigLoc(), DBType.UUID));
        params.add(new StatementParameter(definition.getSrcLoc(), DBType.UUID));
        params.add(new StatementParameter(isValidation, DBType.BOOLEAN));
        int added = executeUpdate("INSERT INTO " + TABLE_NAME() + "(" + ID + ", " + USER + ", " + ASSIGNMENTID + ", "
                + CONFIGLOC + ", " + SRCLOC + ", " + ISVALIDATION + ") VALUES (?, ?, ?, ?, ?, ?);", params);
        if (added > 0) {
            updateStatus(statProps);
        }

        return executeSubmission(definition, statProps);
    }

    private UUID executeSubmission(AssignmentDefinition definition, StatusProperties statProps) throws IOException {
        SubmissionWorker worker = new SubmissionWorker(statProps.getId(), definition, statProps, storageService, this,
                hadoopService);
        threadpool.execute(worker);
        return statProps.getId();
    }

    private AssignmentSubmission getById(UUID id) {
        AssignmentSubmission submission = null;
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(id, DBType.UUID));
        try {
            ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + " WHERE " + ID + "=?;", params);
            if (rs.next()) {
                submission = extractSubmissionProps(rs);
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return submission;
    }

    private List<AssignmentSubmission> getByAssignment(UUID assignmentId) {
        List<AssignmentSubmission> submissions = new ArrayList<AssignmentSubmission>();
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(assignmentId, DBType.UUID));

        try {
            ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + " WHERE " + ASSIGNMENTID + "=?;", params);
            while (rs.next()) {
                submissions.add(extractSubmissionProps(rs));
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return submissions;
    }

    private boolean deleteById(UUID id, boolean deleteFiles) throws ClassNotFoundException, SQLException, IOException {
        AssignmentSubmission existingSubmission = getById(id);
        if (existingSubmission != null) {
            UUID srcLoc = existingSubmission.getSrcLoc();
            UUID configLoc = existingSubmission.getConfigLoc();
            deleteById(TABLE_NAME(), existingSubmission.getId());

            if (deleteFiles) {
                storageService.deleteFile(configLoc);
                storageService.deleteFile(srcLoc);
                hadoopService.delete("/submission/" + existingSubmission.getId());
            }

            return true;
        }
        return false;
    }

    private AssignmentSubmission extractSubmissionProps(ResultSet rs) throws SQLException, IOException {
        UUID id = (UUID) rs.getObject(ID);
        String user = rs.getString(USER);
        UUID configLoc = (UUID) rs.getObject(CONFIGLOC);
        UUID srcLoc = (UUID) rs.getObject(SRCLOC);

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(id);
        submission.setUser(user);
        submission.setConfigLoc(configLoc);
        submission.setSrcLoc(srcLoc);

        if (configLoc != null) {
            GenJobConfiguration gConfig = new GenJobConfiguration(storageService.getFile(configLoc).getAbsolutePath());
            submission.setConfig(gConfig.getconfigProps());
        }
        if (srcLoc != null) {
            submission.setSrcName(storageService.getFile(srcLoc).getName());
        }

        return submission;
    }
}
