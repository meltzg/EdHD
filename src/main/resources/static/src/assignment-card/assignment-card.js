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
        this._set_formattedDate((new Date(this.assignmentProps.dueDate)).toLocaleDateString());
    }
    submit() {
        console.log("TODO submission");
    }
    editAssignment() {
        console.log("TODO edit");
    }
    deleteAssignment() {
        console.log("TODO delete");
    }
    toggle() {
        this.$.collapse_config.toggle();
    }
}

window.customElements.define(AssignmentCard.is, AssignmentCard);