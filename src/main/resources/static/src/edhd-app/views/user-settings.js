class UserSettings extends Polymer.Element {
    static get is() { return 'user-settings'; }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean,
                value: false
            }
        }
    }
    updateAdmin() {
        let adminSettings = {
            isAdmin: this.$.isAdminToggle.checked,
            password: this.$.adminPasswordInput.value
        };
        this.$.updateAdmin.body = adminSettings;
        // This shouldn't be necessary, but there seems to be a bug in iron-ajax related to post
        this.$.updateAdmin.headers = {
            'X-XSRF-TOKEN': document.cookie.match('XSRF-TOKEN.*')[0].split('=')[1]
        }
        let request = this.$.updateAdmin.generateRequest();
        request.completes.then(function (event) {
            location.reload();
        }, function (rejected) {
            this.$.errorToast.fitInto = this;
            this.$.errorToast.show({ text: 'Could not update admin settings.' });
        }.bind(this));
    }
}
customElements.define(UserSettings.is, UserSettings);