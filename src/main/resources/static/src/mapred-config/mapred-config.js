/**
* `mapred-config`
* Form for creating a GenMapred config
*
* @customElement
* @polymer
*/
class MapredConfig extends Polymer.Element {
    static get is() { return 'mapred-config'; }
    static get properties() {
        return {
            standardConfigs: {
                type: Object,
                value: function () { return {}; }
            },
            customConfigs: {
                type: Array,
                value: function () { return []; }
            },
            fullConfig: {
                type: Object,
                notify: true,
                value: function () { return {}; }
            },
            primaryConfig: {
                type: Object,
                value: function () { return {}; }
            },
            _oldPrimaryConfig: {
                type: Object,
                readOnly: true,
                value: function () { return {}; }
            },
            _primaryCustomConfigs: {
                type: Object,
                readOnly: true,
                value: function () { return {}; }
            },
            _standardConfigs: {
                type: Array,
                readOnly: true,
                value: [
                    "mapperClass",
                    "partitionerClass",
                    "sortComparatorClass",
                    "groupingComparatorClass",
                    "combinerComparatorClass",
                    "combinerClass",
                    "reducerClass",
                    "outputKeyClass",
                    "outputValueClass",
                    "inputPath"
                ]
            }
        };
    }
    static get observers() {
        return [
            'buildConfig(standardConfigs.*, customConfigs.*)',
            'reconcilePrimary(primaryConfig.*)'
        ];
    }
    addCustomConf(e) {
        if (e.keyCode !== 13) {
            return;
        }
        if (!this.$.customConfName.value || this.$.customConfName.value.length <= 0) {
            this.showError('Custom Configuration Name must be at least 1 character!');
            return;
        }

        let confName = this.$.customConfName.value;
        if (this.customConfigs.filter(conf => (conf.name === confName)).length) {
            this.showError('Custom Configuration with name "' + confName + '" already exists!');
            return;
        }
        if (this._standardConfigs.includes(confName)) {
            this.showError(confName + ' is a reserved configuration name');
            return;
        }
        if (this._primaryCustomConfigs.hasOwnProperty(confName) && !this._primaryCustomConfigs[confName].isAppendable) {
            this.showError('Cannot add non-appendable primary, custom configuration.');
            return;
        }

        this.$.customConfName.value = '';
        this.push('customConfigs', {
            name: confName,
            isAppendable: false,
            value: null
        });
    }
    removeCustomConf(e) {
        for (let i = 0; i < this.customConfigs.length; i++) {
            if (this.customConfigs[i].name === e.model.item.name) {
                this.splice('customConfigs', i, 1);
            }
        }
    }
    buildConfig(standardConfigs, customConfigs) {
        let fullConfig = {}

        this._standardConfigs.forEach(function (config) {
            if (!(this.primaryConfig[config] && this.isValidVal(this.primaryConfig[config].val)) && this.isValidVal(this.standardConfigs[config])) {
                fullConfig[config] = {
                    val: this.standardConfigs[config]
                };
            }
        }.bind(this));

        this.customConfigs.forEach(function (config) {
            if (this.isValidVal(config.value)) {
                fullConfig[config.name] = {
                    val: config.value,
                    isAppendable: config.isAppendable
                }
            }
        }.bind(this));

        // console.log(JSON.stringify(fullConfig, null, 2));
        this.set('fullConfig', fullConfig);
    }
    reconcilePrimary(primaryConfig) {
        this._standardConfigs.forEach(function (config) {
            if (this.primaryConfig[config] && this.isValidVal(this.primaryConfig[config].val)) {
                this.set('standardConfigs.' + config, this.primaryConfig[config].val);
            } else if (!(this.primaryConfig[config] && this.isValidVal(this.primaryConfig[config].val)) &&
                (this._oldPrimaryConfig[config] && this.isValidVal(this._oldPrimaryConfig[config].val))) {
                // a standard config was removed
                this.set('standardConfigs.' + config, '');
            }
        }.bind(this));
        // resetting the entire object shouldn't be necessary, but the values
        // won't update in the UI correctly otherwise
        let temp = JSON.parse(JSON.stringify(this.standardConfigs));
        this.set('standardConfigs', {});
        this.set('standardConfigs', temp);

        let primatyCustomConfigs = {};
        let nonAppendables = [];
        for (let config in this.primaryConfig) {
            if (!this._standardConfigs.includes(config)) {
                primatyCustomConfigs[config] = this.primaryConfig[config];
                if (!this.primaryConfig[config].isAppendable) {
                    nonAppendables.push(config);
                }
            }
        }
        // remove the non-appendable primary configs from this
        this.set('customConfigs', this.customConfigs.filter(conf => (!nonAppendables.includes(conf.name))));

        this._set_primaryCustomConfigs(primatyCustomConfigs);
        this._set_oldPrimaryConfig(JSON.parse(JSON.stringify(this.primaryConfig)));
    }
    isValidVal(val) {
        return val !== undefined && val !== null && val.length > 0;
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.positionTarget = this.$.top;
        this.$.errorToast.show({ text: msg });
    }
    getFile() {
        if (this.$.srcZip.files.length) {
            return this.$.srcZip.files[0];
        }
        return null;
    }
    _toArray(obj) {
        return Object.keys(obj).map(function (key) {
            return {
                name: key,
                value: obj[key]
            };
        });
    }
    _hasPrimaryCustoms(primaryCustomConfigs) {
        return Object.keys(primaryCustomConfigs).length > 0;
    }
}

window.customElements.define(MapredConfig.is, MapredConfig);