class HDFSBrowser extends Polymer.Element {
    static get is() {
        return 'hdfs-browser';
    }
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
            },
            previewLocation: {
                type: String,
                value: null
            },
            previewContent: {
                type: String,
                value: null
            }
        };
    }
    static get observers() {
        return ['getHDFSDir(location)'];
    }
    getHDFSDir(path) {
        if (path) {
            this.isBusy = true;
            // double encode to avoid spring barfing on encoded slash
            this.$.requestHDFS.url = '/hdfs/ls/' + btoa(path);
            let request = this.$.requestHDFS.generateRequest();
            request.completes.then(function (event) {
                let data = event.response;
                this.set('hdfsChildren', data.children);
                this.location = data.location;
                this.isBusy = false;
            }.bind(this), function () {
                this.isBusy(false);
            }.bind(this));
        }
    }
    getHDFSPreview(path) {
        this.previewLocation = path;
        this.previewContent = null;
        this.isBusy = true;
        // double encode to avoid spring barfing on encoded slash
        this.$.requestPreview.url = '/hdfs/preview/' + btoa(path);
        let request = this.$.requestPreview.generateRequest();
        request.completes.then(function (event) {
            let data = event.response;
            this.previewContent = data.preview;
            this.isBusy = false;
        }.bind(this), function () {
            this.isBusy = false;
        }.bind(this));
    }
    refresh() {
        this.getHDFSDir(this.location);
    }
    goUp() {
        let parent = this.location.substr(0, this.location.lastIndexOf('/')) || '/';
        this.getHDFSDir(parent);
    }
    mkDir(e) {
        if (e.keyCode !== 13) {
            return;
        }
        let dir = this.shadowRoot.querySelector('#mkdir').value;
        if (dir) {
            dir = btoa(dir);
            let location = btoa(this.location);
            this.$.requestMkDir.url = '/hdfs/mkdir/' + location + '/' + dir;
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
        path = btoa(path);
        this.$.requestRm.url = '/hdfs/rm/' + path;
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
        let fileInfo = event.model.__data.item;
        if (fileInfo.directory) {
            this.getHDFSDir(fileInfo.path);
        } else {
            this.getHDFSPreview(fileInfo.path);
        }
    }
    download(event) {
        let fileInfo = event.model.__data.item;
        if (!fileInfo.directory) {
            this.$.requestFile.url = '/hdfs/get/' + btoa(fileInfo.path);
            this.isBusy = true;
            let request = this.$.requestFile.generateRequest();
            request.completes.then(function (event) {
                let data = event.response;
                let header = event.xhr.getResponseHeader('content-disposition').split(';').map(elem => elem.split('='));
                // header = header.reduce(function (prev, curr) {
                //     if (curr.length === 2) {
                //         prev[curr[0]] = curr[1];
                //     }
                // });
                // let lastSlash = header.lastIndexOf('/');
                // if (lastSlash != -1) {
                //     header = header.substr(lastSlash + 1);
                // }
                // console.log(header);
                this.isBusy = false;
            }.bind(this), function () {
                this.isBusy = false;
            }.bind(this));
        }
    }
}
customElements.define(HDFSBrowser.is, HDFSBrowser);