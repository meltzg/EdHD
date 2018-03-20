class LoginView extends Polymer.Element {
    static get is() { return 'login-view'; }
    static get properties() {
        return {
            credentials: {
                type: Object,
                value: function () {
                    return {
                        username: null,
                        password: null
                    };
                }
            },
            loginEnabled: {
                type: Boolean,
                value: false
            }
        };
    }
    static get observers() {
        return ['enableLogin(credentials.*)'];
    }
    enableLogin() {
        this.loginEnabled = this.credentials.username && this.credentials.password;
    }
    submitLogin() {
        this.$.requestLogin.params = this.credentials;
        let request = this.$.requestLogin.generateRequest();
        request.completes.then(function () {
            location.reload();
        }.bind(this), function () {
            this.showError('Login failed!');
        }.bind(this));
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({ text: msg });
    }
}
customElements.define(LoginView.is, LoginView);