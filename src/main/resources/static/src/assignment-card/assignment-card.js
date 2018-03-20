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
        return ['updateDueDate(assignmentProps.dueDate)'];
    }
    updateDueDate() {
        this._set_formattedDate(moment(this.assignmentProps.dueDate * 1000).format('lll'));
    }
    submit() {
        // TODO submission
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
}

window.customElements.define(AssignmentCard.is, AssignmentCard);