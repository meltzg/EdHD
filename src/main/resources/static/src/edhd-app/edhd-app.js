/**
         * @customElement
         * @polymer
         */
class EdhdApp extends Polymer.Element {
    static get is() { return 'edhd-app'; }
    static get properties() {
        return {
            _userInfo: {
                type: Object,
                value: null,
                readOnly: true
            },
            _isAdmin: {
                type: Boolean,
                value: false,
                readOnly: true
            },
            page: {
                type: String,
                reflectToAttribute: true,
                observer: '_pageChanged',
            }
        };
    }
    ready() {
        super.ready();
        let request = this.$.requestUser.generateRequest();
        request.completes.then(this.handleUser.bind(this), this.handleUserError.bind(this));
        this.addEventListener('reload-assignments', this.handleReloadAssignments.bind(this));
        this.addEventListener('edit-assignment', this.handleEditAssignment.bind(this));
    }
    sendLogout() {
        this.$.logout.generateRequest();
    }
    handleUser(event) {
        let userInfo = event.__data.response;
        this._set_userInfo(userInfo);
        let request = this.$.isAdmin.generateRequest();
    }
    handleUserError(rejected) {
        this._set_userInfo(null);
        console.log(rejected);
    }
    handleLogout(event) {
        location.reload();
    }
    handleIsAdmin(event) {
        this._set_isAdmin(event.detail.response.isAdmin);
    }
    openSettings() {
        this.page = 'user-settings';
    }
    handleReloadAssignments(event) {
        this.page = 'all-assignments';
        this.$.assignments.refresh();
    }
    handleEditAssignment(event) {
        if (this._isAdmin) {
            this.page = 'create-assignment';
            this.$.create_assignment.editId = event.detail.id;
        }
    }
    _pageChanged(page) {
        // Load page import on demand. Show 404 page if fails
        const resolvedPageUrl = this.resolveUrl('views/' + page + '.html');
        Polymer.importHref(
            resolvedPageUrl,
            null,
            this._showPage404.bind(this),
            true);
    }
    _showPage404() {
        this.page = 'all-assignments';
    }
}
window.customElements.define(EdhdApp.is, EdhdApp);