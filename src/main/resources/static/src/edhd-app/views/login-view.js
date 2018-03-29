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
            signup: {
                type: Object,
                value: function () {
                    return {
                        username: null,
                        password: null,
                        matchingPassword: null
                    };
                }
            },
            loginEnabled: {
                type: Boolean,
                value: false
            },
            signupEnabled: {
                type: Boolean,
                value: false
            }
        };
    }
    static get observers() {
        return [
            'enableLogin(credentials.*)',
            'enableSignup(signup.*)'
        ];
    }
    enableLogin() {
        this.loginEnabled = this.credentials.username && this.credentials.password;
    }
    enableSignup() {
        this.signupEnabled = this.signup.username && this.signup.password && this.signup.password === this.signup.matchingPassword;
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
    submitSignup() {
        this.$.requestRegister.body = this.signup;
        let request = this.$.requestRegister.generateRequest();
        request.completes.then(function(event) {
            let response = event.response;
            if (!response.success) {
                this.showError('User already exists!');
            } else {
                this.credentials.username = this.signup.username;
                this.credentials.password = this.signup.password;
                this.submitLogin();
            }
        }.bind(this), function() {
            this.showError('Signup failed!');
        }.bind(this));
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({ text: msg });
    }
}
customElements.define(LoginView.is, LoginView);