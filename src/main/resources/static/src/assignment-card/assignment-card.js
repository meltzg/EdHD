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
            _formattedDate: {
                type: String,
                readOnly: true,
                value: null
            }
        };
    }
    static get observers() {
        return ['updateDueDate(assignmentProps.dueDate)']
    }
    updateDueDate(dueDate) {
        this._set_formattedDate(moment(this.assignmentProps.dueDate).format('lll'));
    }
    submit() {
        console.log("TODO submission");
    }
    editAssignment() {
        this.dispatchEvent(new CustomEvent('edit-assignment', { bubbles: true, composed: true, detail: { id: this.assignmentProps.id } }));
    }
    deleteAssignment() {
        // This shouldn't be necessary, but there seems to be a bug in iron-ajax related to not GET
        this.$.delete.headers = {
            'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
        }
        let request = this.$.delete.generateRequest();
        request.completes.then(function (event) {
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
        }.bind(this), function (rejected) {
            this.dispatchEvent(new CustomEvent('reload-assignments', { bubbles: true, composed: true }));
        }.bind(this));
    }
    toggle() {
        this.$.collapse_config.toggle();
    }
}

window.customElements.define(AssignmentCard.is, AssignmentCard);