class CreateAssignment extends Polymer.Element {
    static get is() { return 'create-assignment'; }
    static get properties() {
        return {
            primaryConfig: {
                type: Object
            },
            selectedConf: {
                type: Number,
                value: 0,
                observer: 'doop'
            }
        };
    }
    doop() {
        console.log(this.selectedConf)
    }
}
customElements.define(CreateAssignment.is, CreateAssignment);