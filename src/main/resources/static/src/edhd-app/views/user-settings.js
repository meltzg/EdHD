class UserSettings extends Polymer.Element {
    static get is() { return 'user-settings'; }
    static get properties() {
        return {
            isAdmin: {
                type: Boolean,
                value: false
            }
        };
    }
    updateAdmin() {
        let adminSettings = {
            isAdmin: this.$.isAdminToggle.checked,
            password: this.$.adminPasswordInput.value
        };
        this.$.updateAdmin.body = adminSettings;
        let request = this.$.updateAdmin.generateRequest();
        request.completes.then(function () {
            location.reload();
        }, function () {
            this.$.errorToast.fitInto = this;
            this.$.errorToast.show({ text: 'Could not update admin settings.' });
        }.bind(this));
    }
}
customElements.define(UserSettings.is, UserSettings);