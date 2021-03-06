RxEvent : Event {
	var <>metadata;
	
	dispatch { arg type, payload;
		// implement this to act as an event-source
		Dispatcher(type, payload, this)
	}

	listen { arg type, fn;
		Dispatcher.addListener(type, this, { arg payload;
			if (payload.id == this.at('id')) {
				fn.value(payload)
			}
		})
	}

	*new { arg event;
		if (event.class == RxEvent) {
			^event;
		};
		^super.new.init(event)
	}

	init { arg event;
		metadata = event.parent !? {
			event.parent['metadata']
		};

		know = true;
		event.keysValuesDo { arg key, val;
			super.put(key, val);
		};

		this.listen(Topics.objectUpdated, { arg payload;
			this.putAll(payload, false)
		});

		this.listen(Topics.moduleReload, { arg payload;
			if (payload.path == metadata.path) {
				parent = this.getParentFromMetadata(metadata);
			}
		})
	}

	parent {
		metadata !? {
			parent = this.getParentFromMetadata(metadata);
			^parent;
		} ?? {
			^super.parent;
		}
	}

	parent_ { arg parentEvent;
		if (parentEvent.isNil) {
			^this
		};

		parentEvent.metadata !? {
			metadata = parentEvent.metadata;
		};

		parent = parentEvent;
	}

	getParentFromMetadata { arg md;
		^Mod(md.path).at(md.memberKey);
	}

	put { arg key, value, dispatch = true;
		var originalValue = this.at(key);

		super.put(key, value);

		if (dispatch && originalValue != value) {
			this.dispatch(
				type: Topics.objectUpdated,
				payload: (id: this.id).put(key, value),
			)
		};
		^this;
	}

	putAll { arg dictionary, dispatch = true;
		var updates = ();

		dictionary.keysValuesDo { arg key, value;
			if (this.at(key) != value) {
				updates.put(key, value);
			};
			
			super.put(key, value);
		};


		if (updates.size > 0 && dispatch) {
			this.dispatch(
				type: Topics.objectUpdated,
				payload: updates.putAll((id: this.id))
			)
		}
	}

	id {
		^this['id']
	}

	// == { arg that;

		
	// }
}


V : RxEvent {}