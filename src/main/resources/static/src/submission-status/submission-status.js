class SubmissionStatus extends Polymer.Element {
    static get is() { return 'submission-status'; }
    static get properties() {
        return {
            statusProps: {
                type: Object,
                value: function () {
                    return {};
                }
            },
            isComplete: {
                type: Boolean,
                computed: 'computeIsComplete(statusProps.completeStatus)'
            }
        };
    }
    static get observers() {
        return [];
    }
    getStatClass(status) {
        switch (status) {
        case 'FAIL':
            return 'fail';
        case 'COMPLETE':
            return 'complete';
        case 'SUCCESS':
            return 'success';
        default:
            return 'pending';
        }
    }
    computeIsComplete() {
        return this.statusProps.completeStatus !== 'PENDING';
    }
    showCompileStatus(element) {
        this.$.compileMsg.positionTarget = element;
        this.$.compileMsg.open();
    }
    showRunStatus(element) {
        this.$.runMsg.positionTarget = element;
        this.$.runMsg.open();
    }
    showValidateStatus(element) {
        this.$.validateMsg.positionTarget = element;
        this.$.validateMsg.open();
    }
    showCompleteStatus(element) {
        this.$.completeMsg.positionTarget = element;
        this.$.completeMsg.open();
    }
}

window.customElements.define(SubmissionStatus.is, SubmissionStatus);