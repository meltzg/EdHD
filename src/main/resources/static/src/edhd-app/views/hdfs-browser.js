class HDFSBrowser extends Polymer.Element {
    static get is() { return 'hdfs-browser'; }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean
            },
            location: {
                type: String,
                value: '/'
            },
            hdfsChildren: {
                type: Array,
                value: function () {
                    return [];
                }
            },
            file: {
                type: Object,
                value: null
            },
            hasFile: {
                type: Boolean,
                value: false
            },
            isBusy: {
                type: Boolean,
                value: false
            }
        };
    }
    static get observers() {
        return ['getHDFS(location)'];
    }
    getHDFS(path) {
        if (path) {
            this.isBusy = true;
            // double encode to avoid spring barfing on encoded slash
            this.$.requestHDFS.url = '/hdfs-ls/' + encodeURIComponent(encodeURIComponent(path));
            let request = this.$.requestHDFS.generateRequest();
            request.completes.then(function (event) {
                let data = event.response;
                this.set('hdfsChildren', data.children);
                this.location = data.location;
                this.isBusy = false;
            }.bind(this));
        }
    }
    refresh() {
        this.getHDFS(this.location);
    }
    goUp() {
        let parent = this.location.substr(0, this.location.lastIndexOf('/')) || '/';
        this.getHDFS(parent);
    }
    mkDir(e) {
        if (e.keyCode !== 13) {
            return;
        }
        let dir = this.shadowRoot.querySelector('#mkdir').value;
        if (dir) {
            dir = encodeURIComponent(encodeURIComponent(dir));
            let location = encodeURIComponent(encodeURIComponent(this.location));
            // This shouldn't be necessary, but there seems to be a bug in iron-ajax related to not GET
            this.$.requestMkDir.headers = {
                'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
            };
            this.$.requestMkDir.url = '/hdfs-mkdir/' + location + '/' + dir;
            let request = this.$.requestMkDir.generateRequest();
            this.isBusy = true;
            request.completes.then(function () {
                this.refresh();
            }.bind(this), function () {
                this.refresh();
            }.bind(this));
        }
    }
    remove(e) {
        let path = e.model.__data.item.path;
        path = encodeURIComponent(encodeURIComponent(path));
        this.$.requestRm.headers = {
            'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
        };
        this.$.requestRm.url = '/hdfs-rm/' + path;
        let request = this.$.requestRm.generateRequest();
        this.isBusy = true;
        request.completes.then(function () {
            this.refresh();
        }.bind(this), function () {
            this.refresh();
        }.bind(this));
    }
    sendFile() {
        let formData = new FormData();
        formData.append('location', new Blob([JSON.stringify({
            location: this.location
        })], {
            type: 'application/json'
        }));
        formData.append('file', this.file);
        this.$.requestPutFile.body = formData;
        this.$.requestPutFile.contentType = null;
        // This shouldn't be necessary, but there seems to be a bug in iron-ajax related to post
        this.$.requestPutFile.headers = {
            'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
        };
        let request = this.$.requestPutFile.generateRequest();
        this.isBusy = true;
        request.completes.then(function () {
            this.refresh();
        }.bind(this), function () {
            this.refresh();
        }.bind(this));
    }
    fileChange() {
        let fileElem = this.shadowRoot.querySelector('#file');
        if (fileElem.files.length) {
            this.set('file', fileElem.files[0]);
            this.hasFile = true;
        } else {
            this.set('file', null);
            this.hasFile = false;
        }
    }
    handleURLSelect(event) {
        this.getHDFS(event.model.__data.item.path);
    }
}
customElements.define(HDFSBrowser.is, HDFSBrowser);