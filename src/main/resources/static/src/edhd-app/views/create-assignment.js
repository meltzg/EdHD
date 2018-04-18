class CreateAssignment extends Polymer.Element {
    static get is() { return 'create-assignment'; }
    static get properties() {
        return {
            assignmentId: {
                type: String,
                value: null
            },
            assignmentName: {
                type: String
            },
            assignmentDesc: {
                type: String
            },
            dueDate: {
                type: String
            },
            primaryConfig: {
                type: Object
            },
            config: {
                type: Object
            },
            primarySrcName: {
                type: String
            },
            srcName: {
                type: String
            },
            editId: {
                type: String
            },
            _isBusy: {
                type: Boolean,
                readOnly: true,
                value: false
            },
            _dueUnixDate: {
                type: Number,
                computed: 'dateToUnix(dueDate)'
            },
            _assignmentProps: {
                type: Object,
                value: function () {
                    return {
                        id: null,
                        dueDate: null,
                        name: null,
                        desc: null,
                        primaryConfig: null,
                        config: null
                    };
                }
            },
            _selectedConf: {
                type: String
            }
        };
    }
    static get observers() {
        return [
            'computeProps(id, assignmentName, assignmentDesc, _dueUnixDate, primaryConfig.*, config.*, primarySrcName, srcName)',
            'editAssignment(editId)'
        ];
    }
    dateToUnix(date) {
        if (date) {
            return moment(date).unix();
        }
        return 0;
    }
    computeProps() {
        this.set('_assignmentProps', {
            id: this.assignmentId,
            dueDate: this._dueUnixDate,
            name: this.assignmentName,
            desc: this.assignmentDesc,
            primaryConfig: this.primaryConfig || {},
            config: this.config || {},
            primarySrcName: this.primarySrcName,
            srcName: this.srcName
        });
    }
    submitCreateAssignment() {
        let formData = new FormData();
        formData.append('properties', new Blob([JSON.stringify(this._assignmentProps)], {
            type: 'application/json'
        }));

        let primaryElem = this.$.primary;
        let secondaryElem = this.$.secondary;

        let primaryFile = primaryElem ? primaryElem.getFile() : null;
        let secondaryFile = secondaryElem ? secondaryElem.getFile() : null;

        if (primaryFile) {
            formData.append('primarySrc', primaryFile);
        }
        if (secondaryFile) {
            formData.append('secondarySrc', secondaryFile);
        }

        this.$.createAssignment.body = formData;
        this.$.createAssignment.contentType = null;
        let request = this.$.createAssignment.generateRequest();
        request.completes.then(function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
            this.reset();
        }.bind(this), function (rejected) {
            this.showError('Error creating assignment ' + rejected);
        }.bind(this));
    }
    reset() {
        this.assignmentId = null;
        this.assignmentName = '';
        this.assignmentDesc = '';
        this.dueDate = null;
        this.primaryConfig = {};
        this.config = {};

        let primary = this.$.primary;
        let secondary = this.$.secondary;
        if (primary) {
            primary.reset();
        }
        if (secondary) {
            secondary.reset();
        }

    }
    editAssignment(id) {
        if (this.editId && this.editId.length > 0) {
            this._set_isBusy(true);
            this.reset();
            this.$.getAssignment.url = '/assignment/get/' + this.editId;
            let request = this.$.getAssignment.generateRequest();
            request.completes.then(function (event) {
                let assignment = event.response;
                this.assignmentId = assignment.id;
                this.assignmentName = assignment.name;
                this.assignmentDesc = assignment.desc;
                this.$.date.value = moment(assignment.dueDate * 1000).format('YYYY-MM-DD');
                this.$.primary.setConfig(assignment.primaryConfig, assignment.primarySrcName);
                this.$.secondary.setConfig(assignment.config, assignment.srcName);
                this._set_isBusy(false);
            }.bind(this), function () {
                this._set_isBusy(false);
                this.showError('An error occured while retrieveing assignment ' + id);
            }.bind(this));
        }
        this.editId = null;
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({ text: msg });
    }
}
customElements.define(CreateAssignment.is, CreateAssignment);