class AssignmentCard extends Polymer.Element {
    static get is() { return 'assignment-card'; }
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
            }
        };
    }
    static get observers() {
        return [
            'computeSubmission(assignmentProps.*, submissionProps.*)',
            'updateDueDate(assignmentProps.dueDate)'
        ];
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
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
        }.bind(this), function (rejected) {
            this.showError('Error submitting assignment ' + rejected);
        }.bind(this));
    }
    editAssignment() {
        this.dispatchEvent(new CustomEvent('edit-assignment', { bubbles: true, composed: true, detail: { id: this.assignmentProps.id } }));
    }
    deleteAssignment() {
        let request = this.$.delete.generateRequest();
        request.completes.then(function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
        }.bind(this), function () {
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
        }.bind(this));
    }
    toggle() {
        this.$.collapse_config.toggle();
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({ text: msg });
    }
}

window.customElements.define(AssignmentCard.is, AssignmentCard);