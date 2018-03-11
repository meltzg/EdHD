class CreateAssignment extends Polymer.Element {
    static get is() { return 'create-assignment'; }
    static get properties() {
        return {
            id: {
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
                    }
                }
            },
            _selectedConf: {
                type: Number,
                value: 0
            }
        };
    }
    static get observers() {
        return ['computeProps(id, assignmentName, assignmentDesc, _dueUnixDate, primaryConfig.*, config.*)'];
    }
    dateToUnix(date) {
        if (date) {
            return Date.parse(date);
        }
        return 0;
    }
    computeProps(id, assignmentName, assignmentDesc, _dueUnixDate, primaryConfig, config) {
        this.set('_assignmentProps', {
            id: this.id,
            dueDate: this._dueUnixDate,
            name: this.assignmentName,
            desc: this.assignmentDesc,
            primaryConfig: this.primaryConfig || {},
            config: this.config || {}
        });
    }
    submitCreateAssignment() {
        let formData = new FormData();
        formData.append('properties', new Blob([JSON.stringify(this._assignmentProps)], {
            type: 'application/json'
        }));

        let primaryElem = this.shadowRoot.querySelector('#primary');
        let secondaryElem = this.shadowRoot.querySelector('#secondary')
        
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
        // This shouldn't be necessary, but there seems to be a bug in iron-ajax related to post
        this.$.createAssignment.headers = {
            'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
        }
        let request = this.$.createAssignment.generateRequest();
    }
}
customElements.define(CreateAssignment.is, CreateAssignment);