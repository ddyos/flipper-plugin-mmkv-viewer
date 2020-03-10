/**
 * Copyright 2018-present Facebook.
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 * @format
 */

import {
  Heading,
  FlexColumn,
  colors,
  FlexRow,
  ManagedDataInspector,
  styled,
  Select,
  Button,
} from 'flipper';
import {FlipperPlugin} from 'flipper';

const {clone, set} = require('lodash');

type SharedPreferencesChangeEvent = {|
  preferences: string,
  name: string,
  time: number,
  deleted: boolean,
  value: string,
|};

export type SharedPreferences = {|
  [name: string]: any,
|};

type SharedPreferencesEntry = {
  preferences: SharedPreferences,
  changesList: Array<SharedPreferencesChangeEvent>,
};

type SharedPreferencesMap = {
  [name: string]: SharedPreferencesEntry,
};

type SharedPreferencesState = {|
  selectedPreferences: ?string,
  sharedPreferences: SharedPreferencesMap,
|};

const InspectorColumn = styled(FlexColumn)({
  flexGrow: 0.2,
});

const RootColumn = styled(FlexColumn)({
  paddingLeft: '16px',
  paddingRight: '16px',
  paddingTop: '16px',
});

const ButtonGroupContainer = styled(FlexRow)({
  paddingLeft: '16px',
  paddingRight: '16px',
  paddingBottom: '16px',
});

export default class extends FlipperPlugin<SharedPreferencesState> {
  state = {
    selectedPreferences: null,
    sharedPreferences: {},
  };

  reducers = {
    UpdateSharedPreferences(state: SharedPreferencesState, results: Object) {
      const update = results.update;
      const entry = state.sharedPreferences[update.name] || {changesList: []};
      entry.preferences = update.preferences;
      state.sharedPreferences[update.name] = entry;
      return {
        selectedPreferences: state.selectedPreferences || update.name,
        sharedPreferences: state.sharedPreferences,
      };
    },

    ChangeSharedPreferences(state: SharedPreferencesState, event: Object) {
      const change = event.change;
      const entry = state.sharedPreferences[change.preferences];
      if (entry == null) {
        return state;
      }
      if (change.deleted) {
        delete entry.preferences[change.name];
      } else {
        entry.preferences[change.name] = change.value;
      }
      entry.changesList = [change, ...entry.changesList];
      return {
        selectedPreferences: state.selectedPreferences,
        sharedPreferences: state.sharedPreferences,
      };
    },

    UpdateSelectedSharedPreferences(
      state: SharedPreferencesState,
      event: Object,
    ) {
      return {
        selectedPreferences: event.selected,
        sharedPreferences: state.sharedPreferences,
      };
    },
  };

  init() {
    this.refresh();
  }

  onSharedPreferencesChanged = (path: Array<string>, value: any) => {
    const {selectedPreferences, sharedPreferences} = this.state;
    if (!selectedPreferences || !sharedPreferences[selectedPreferences]) {
      return null;
    }
    const entry = sharedPreferences[selectedPreferences];
    const values = entry.preferences;
    let newValue = value;
    if (path.length > 1 && values) {
      newValue = set(values[path[0]], path.slice(1), value);
    }
    this.client
      .call('setSharedPreference', {
        sharedPreferencesName: this.state.selectedPreferences,
        preferenceName: path[0],
        preferenceValue: newValue,
      })
      .then((results: SharedPreferences) => {
        const update = {
          name: this.state.selectedPreferences,
          preferences: results,
        };
        this.dispatchAction({update, type: 'UpdateSharedPreferences'});
      });
  };

  onSharedPreferencesSelected = (selected: string) => {
    this.dispatchAction({
      selected: selected,
      type: 'UpdateSelectedSharedPreferences',
    });
  };

  refresh = () => {
    this.client
      .call('getAllSharedPreferences')
      .then((results: {[name: string]: SharedPreferences}) => {
        Object.entries(results).forEach(([name, prefs]) => {
          const update = {name: name, preferences: prefs};
          this.dispatchAction({update, type: 'UpdateSharedPreferences'});
        });
      });

    this.client.subscribe(
      'sharedPreferencesChange',
      (change: SharedPreferencesChangeEvent) => {
        this.dispatchAction({change, type: 'ChangeSharedPreferences'});
      },
    );
  };

  render() {
    const {selectedPreferences, sharedPreferences} = this.state;
    if (!selectedPreferences || !sharedPreferences[selectedPreferences]) {
      return null;
    }
    const entry = sharedPreferences[selectedPreferences];

    let parsed = entry.preferences || {};
    try {
      for (const key in parsed) {
        try {
          parsed[key] = JSON.parse(parsed[key]);
          for (const k in parsed[key]) {
            parsed[key][k] = JSON.parse(parsed[key][k]);
          }
        } catch (err) {}
      }
    } catch (err) {}

    return (
      <RootColumn grow={true}>
        <Heading>
          <span style={{marginRight: '16px'}}>MMKV File</span>
          <Select
            options={Object.keys(this.state.sharedPreferences)
              .sort((a, b) => (a.toLowerCase() > b.toLowerCase() ? 1 : -1))
              .reduce((obj, item) => {
                obj[item] = item;
                return obj;
              }, {})}
            selected={selectedPreferences}
            onChange={this.onSharedPreferencesSelected}
          />
        </Heading>
        <ButtonGroupContainer>
          <Button onClick={this.refresh} compact={true}>
            Refresh
          </Button>
        </ButtonGroupContainer>
        <FlexRow grow={true} scrollable={true}>
          <InspectorColumn>
            <Heading>
              <span style={{marginRight: '16px'}}>Inspector</span>
            </Heading>
            <ManagedDataInspector
              data={parsed}
              setValue={this.onSharedPreferencesChanged}
              collapsed
            />
          </InspectorColumn>
        </FlexRow>
      </RootColumn>
    );
  }
}
