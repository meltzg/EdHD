/**
        * `mapred-config`
        * Form for creating a GenMapred config
        *
        * @customElement
        * @polymer
        * @demo demo/index.html
        */
class MapredConfig extends Polymer.Element {
    static get is() { return 'mapred-config'; }
    static get properties() {
        return {
            standardConfigs: {
                type: Object,
                value: {}
            },
            customConfigs: {
                type: Array,
                value: []
            },
            fullConfig: {
                type: Object,
                notify: true
            },
            primaryConfig: {
                type: Object,
                value: {}
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
            }
        }.bind(this));
        let temp = JSON.parse(JSON.stringify(this.standardConfigs));
        this.set('standardConfigs', {});
        this.set('standardConfigs', temp);
        console.log();
    }
    isValidVal(val) {
        return val !== undefined && val !== null && val.length > 0;
    }
    showError(msg) {
        this.$.errorToast.fitInto = this;
        this.$.errorToast.show({ text: msg });
    }
}

window.customElements.define(MapredConfig.is, MapredConfig);