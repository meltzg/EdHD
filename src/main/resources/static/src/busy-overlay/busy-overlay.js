class BusyOverlay extends Polymer.mixinBehaviors([Polymer.IronOverlayBehavior], Polymer.Element) {
    static get is() { return 'busy-overlay'; }
    static get properties() {
        return {
            isBusy: {
                type: Boolean,
                value: false
            }
        };
    }
    static get observers() {
        return ['busyChanged(isBusy)'];
    }
    busyChanged() {
        if (this.isBusy) {
            this.open();
        } else {
            this.close();
        }
    }
}

window.customElements.define(BusyOverlay.is, BusyOverlay);