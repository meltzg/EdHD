class AllAssignments extends Polymer.Element {
    static get is() { return 'all-assignments'; }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean,
                value: false
            },
            assignments: {
                type: Array,
                value: function() {
                    return {};
                }
            }
        };
    }
    ready() {
        super.ready();
        this.refresh();
    }
    refresh() {
        let request = this.$.refreshRequest.generateRequest();
        this.set('assignments', []);
        request.completes.then(function(event) {
            this.set('assignments', event.__data.response);
        }.bind(this));
    }
}
customElements.define(AllAssignments.is, AllAssignments);