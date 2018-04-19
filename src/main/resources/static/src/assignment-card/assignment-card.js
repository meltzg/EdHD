class AssignmentCard extends Polymer.Element {
    static get is() {
        return 'assignment-card';
    }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean,
                value: false
            },
            assignmentProps: {
                type: Object,
                value: function () {
                    return {};
                }
            },
            submissionProps: {
                type: Object,
                value: function () {
                    return {};
                }
            },
            _submissionProps: {
                type: Object,
                value: function () {
                    return {
                        id: null,
                        config: null
                    };
                }
            },
            _formattedDate: {
                type: String,
                readOnly: true,
                value: null
            },
            submissionIds: {
                type: Array,
                value: function () {
                    return [];
                }
            },
            _submissionStatuses: {
                type: Array,
                value: function () {
                    return [];
                }
            },
            validatorStatus: {
                type: Object,
                value: function () {
                    return {};
                }
            },
            submissionStatuses: {
                type: Array,
                value: function () {
                    return [];
                }
            },
            _statusInterval: {
                type: Object,
                readOnly: true,
                value: null
            }
        };
    }
    static get observers() {
        return [
            'computeSubmission(assignmentProps.*, submissionProps.*)',
            'updateDueDate(assignmentProps.dueDate)',
            'assignmentPropsChanged(assignmentProps.*)',
            'submissionIdsChanged(submissionIds.*)',
            'statusesChanged(_submissionStatuses.*)'
        ];
    }
    ready() {
        super.ready();
        this.refreshSubmissionIds();
    }
    computeSubmission() {
        this.set('_submissionProps', {
            id: this.assignmentProps.id,
            config: this.submissionProps || {}
        });
    }
    updateDueDate() {
        this._set_formattedDate(moment(this.assignmentProps.dueDate * 1000).format('lll'));
    }
    assignmentPropsChanged() {
        this.refreshSubmissionIds();
    }
    submissionIdsChanged() {
        if (this._statusInterval) {
            clearInterval(this._statusInterval);
        }
        this.refreshStatusInfo();
        this._set_statusInterval(setInterval(function () {
            this.refreshStatusInfo();
        }.bind(this), 5000));
    }
    statusesChanged() {
        let validator = this._submissionStatuses.filter(stat => stat.validation)[0];
        let submissions = this._submissionStatuses.filter(stat => !stat.validation);
        if (validator) {
            this.set('validatorStatus', validator);
        } else {
            this.set('validatorStatus', {});
        }
        this.set('submissionStatuses', submissions);
    }
    refreshStatusInfo() {
        this.$.getSubmissionStatuses.body = this.submissionIds;
        let request = this.$.getSubmissionStatuses.generateRequest();
        request.completes.then(function (event) {
            this.set('_submissionStatuses', event.__data.response);
            let hasPending = false;
            for (let i = 0; i < this._submissionStatuses.length; i++) {
                if (this._submissionStatuses[i].completeStatus === 'PENDING') {
                    hasPending = true;
                    break;
                }
            }
            if (!hasPending) {
                clearInterval(this._statusInterval);
            }
        }.bind(this), function (rejected) {
            this.showError(rejected);
            clearInterval(this._statusInterval);
            this.set('_submissionStatuses', []);
        }.bind(this));
    }
    submit() {
        let formData = new FormData();
        formData.append('properties', new Blob([JSON.stringify(this._submissionProps)], {
            type: 'application/json'
        }));

        let submissionConfigElem = this.$.sibmissionConfig;
        let submissionFile = submissionConfigElem ? submissionConfigElem.getFile() : null;

        if (submissionFile) {
            formData.append('src', submissionFile);
        }

        this.$.submitAssignment.body = formData;
        this.$.submitAssignment.contentType = null;
        let request = this.$.submitAssignment.generateRequest();

        request.completes.then(function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', {
                bubbles: true,
                composed: true
            }));
        }.bind(this), function (rejected) {
            this.showError('Error submitting assignment ' + rejected);
        }.bind(this));
    }
    editAssignment() {
        this.dispatchEvent(new CustomEvent('edit-assignment', {
            bubbles: true,
            composed: true,
            detail: {
                id: this.assignmentProps.id
            }
        }));
    }
    deleteAssignment() {
        let request = this.$.delete.generateRequest();
        request.completes.then(function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', {
                bubbles: true,
                composed: true
            }));
        }.bind(this), function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', {
                bubbles: true,
                composed: true
            }));
        }.bind(this));
    }
    downloadSubmissions() {
        let request = this.$.getAssignmentSubmissions.generateRequest();
        request.completes.then(function (event) {
            let data = event.response;
            let contentDisposition = event.xhr.getResponseHeader('content-disposition').split(';').map(elem => elem.split('='));
            let cDispDict = {};
            contentDisposition.forEach(elem => {
                if (elem.length === 2) {
                    cDispDict[elem[0]] = elem[1];
                }
            });

            let filename = cDispDict.filename;
            if (filename) {
                let lastSlash = filename.lastIndexOf('/');
                if (lastSlash != -1) {
                    filename = filename.substr(lastSlash + 1);
                }

            }
            saveAs(data, filename);
        }.bind(this));
    }
    refreshSubmissionIds() {
        if (this.assignmentProps.id) {
            let request = this.$.getSubmissionIds.generateRequest();
            request.completes.then(function (event) {
                let ids = event.__data.response;
                this.set('submissionIds', ids);
            }.bind(this));
        }
    }
    toggle() {
        this.$.collapse_config.toggle();
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({
            text: msg
        });
    }
}

window.customElements.define(AssignmentCard.is, AssignmentCard);