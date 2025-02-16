Plugin.OurHelloWorldPlugin = class OurHelloWorldPlugin {
		
	/*    
    {
		"event": {
	        "eventName": "MY_EXAMPLE_SHOW_DIALOG",
	        "eventPayload": {}
	    }
	}
    */
	
	constructor(pluginService, eventBus) {
		this.pluginService = pluginService;
		this.eventBus = eventBus;
		this.userStore = this.pluginService.getContextInstance('UserStore');
		this.formatters = this.pluginService.getContextInstance('Formatters');
		this.init();
	}
	
	init() {
		this.eventBus.subscribe(this, true);
				
	}
	
	handleEvent(event) {
		const receiptStore = this.pluginService.getContextInstance('ReceiptStore');
		const receiptModel = receiptStore.getReceiptModel();
		switch(event.getType()) {		
			case 'MY_EXAMPLE_SHOW_DIALOG':
				if (receiptModel) {
                    let inputModel = new cco.InputModel();

       this.eventBus.push('SHOW_GENERIC_INPUT', {

                            title:  'Disclaimer ID' ,
                            showKeyboardSwitchButton: true,
                            keyboardType: 'numblock',
                            widthFactor: 0.8,
                            heightFactor: 0.8,
                            'numblockType': 'default',
                            'configuration': [
                            new cco.GenericInputConfiguration('Disclaimer ID', '1', 'Input', inputModel)
                        ],
                        'hideDoneButton': true,
                            'callback': async (positive) => {
                                if (positive) {
                                    let receiptId = receiptModel.id;
                                    let addFld1 = inputModel.getValue();;
                                    this.pluginService.backendPluginEvent('SAVE_DATA', {
                                        'receiptId': receiptId,
                                        'addFld1': addFld1,
                                    });
                                    }
                            }
                        });	
			}
				break;
			}
	}
};

