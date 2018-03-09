class CreateAssignment extends Polymer.Element {
    static get is() { return 'create-assignment'; }
    static get properties() {
        return {
            primaryConfig: {
                type: Object
            }
        };
    }
}
customElements.define(CreateAssignment.is, CreateAssignment);