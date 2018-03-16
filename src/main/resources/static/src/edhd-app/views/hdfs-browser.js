class HDFSBrowser extends Polymer.Element {
    static get is() { return 'hdfs-browser'; }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean
            },
            location: {
                type: String,
                value: "/"
            },
            hdfsChildren: {
                type: Array,
                value: function () {
                    return [];
                }
            }
        };
    }
    static get observers() {
        return ['getHDFS(location)'];
    }
    getHDFS(path) {
        if (path) {
            // double encode to avoid spring barfing on encoded slash
            this.$.requestHDFS.url = "/hdfs-ls/" + encodeURIComponent(encodeURIComponent(path));
            let request = this.$.requestHDFS.generateRequest();
            request.completes.then(function (event) {
                let data = event.response;
                this.set('hdfsChildren', data.children);
                this.location = data.location;
            }.bind(this));
        }
    }
    refresh() {
        this.getHDFS(this.location);
    }
    handleURLSelect(event) {
        this.getHDFS(event.model.__data.item.path);
    }
}
customElements.define(HDFSBrowser.is, HDFSBrowser);