package org.meltzg.edhd.storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
	UUID putFile(MultipartFile file) throws IllegalStateException, IOException;
	UUID putFile(GenJobConfiguration file) throws IOException;
	UUID putFile(File file);
	File getFile(UUID id);
}
