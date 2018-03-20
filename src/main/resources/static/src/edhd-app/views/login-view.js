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
        request.completes.then(function (event) {
            console.log(event);
        }.bind(this), function (rejected) {
            console.log(rejected);
        }.bind(this));
    }
}
customElements.define(LoginView.is, LoginView);