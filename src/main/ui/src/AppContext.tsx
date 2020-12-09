/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import * as React from 'react';
import {createContext, Dispatch, useReducer} from "react";

const defaultQuery = {
  "query": {
    "MatchAllDocs": {},
  },
  "returned_fields": ["*"]
};

const defaultEndPoint = 'http://localhost:9091/indexes';

export enum Views {
  INDICES = "INDICES",
  FIELDS = "FIELDS",
  INGEST = "INGEST",
  QUERY = "QUERY"
}

enum Actions {
  SELECT_INDEX = "SELECT_INDEX",
  SELECT_VIEW = "SELECT_VIEW",
  SELECT_FIELD = "SELECT_FIELD",
  SET_INDEX_JSON = "SET_INDEX_JSON"
}

interface SelectIndexAction {
  type: typeof Actions.SELECT_INDEX,
  index: string | undefined,
  status: object
}

interface SelectViewAction {
  type: typeof Actions.SELECT_VIEW,
  view: Views
}

interface SelectField {
  type: typeof Actions.SELECT_FIELD,
  field: string | undefined
}

interface SetIndexJson {
  type: typeof Actions.SET_INDEX_JSON,
  indexJson: string
}

type StateActions = SelectIndexAction | SelectViewAction | SelectField | SetIndexJson;

// Definition of the state
type StateType = {
  selectedView: Views;
  selectedIndex: string | undefined;
  selectedIndexStatus: object,
  selectedField: string | undefined,
  indexJson: string,
  queryJson: string,
  defaultQuery: object,
  endPoint: string,
  error: string,
}

// Initial value of the App Context state
const defaultState: StateType = {
  selectedView: Views.INDICES,
  selectedIndex: undefined,
  selectedIndexStatus: {},
  selectedField: undefined,
  indexJson: '',
  queryJson: '',
  defaultQuery: defaultQuery,
  endPoint: defaultEndPoint,
  error: '',
}

const initialState = () => {
  let state = defaultState;
  const viewString = localStorage.getItem(Actions.SELECT_VIEW);
  if (viewString != null) {
    const typeViewString = viewString as keyof typeof Views;
    state = {...state, selectedView: Views[typeViewString]}
  }
  const index = localStorage.getItem(Actions.SELECT_INDEX);
  if (index != null)
    state = {...state, selectedIndex: index};
  const field = localStorage.getItem(Actions.SELECT_FIELD);
  if (field != null)
    state = {...state, selectedField: field};
  const indexJson = localStorage.getItem(Actions.SET_INDEX_JSON);
  if (indexJson != null)
    state = {...state, indexJson: indexJson};
  return state;
}

const reducer = (state: StateType, action: StateActions): StateType => {
  let newState: StateType;
  switch (action.type) {
    case Actions.SELECT_INDEX:
      newState = {
        ...state,
        selectedIndex: action.index,
        selectedIndexStatus: action.status
      };
      break;
    case Actions.SELECT_VIEW:
      newState = {
        ...state,
        selectedView: action.view
      };
      break;
    case Actions.SELECT_FIELD:
      newState = {
        ...state,
        selectedField: action.field
      };
      break;
    case Actions.SET_INDEX_JSON:
      newState = {
        ...state,
        indexJson: action.indexJson
      };
      break;
    default:
      return state;
  }
  return newState;
}

class Dispatcher {

  constructor(private readonly dispatch: Dispatch<StateActions>) {
  }

  selectView(view: Views) {
    localStorage.setItem(Actions.SELECT_VIEW, view);
    return this.dispatch({type: Actions.SELECT_VIEW, view});
  }

  selectIndex(index: string | undefined, status: object) {
    if (index != undefined)
      localStorage.setItem(Actions.SELECT_INDEX, index);
    else
      localStorage.removeItem(Actions.SELECT_INDEX);
    return this.dispatch({type: Actions.SELECT_INDEX, index, status});
  }

  selectField(field: string | undefined) {
    if (field != undefined)
      localStorage.setItem(Actions.SELECT_FIELD, field);
    else
      localStorage.removeItem(Actions.SELECT_FIELD);
    return this.dispatch({type: Actions.SELECT_FIELD, field});
  }

  setIndexJson(indexJson: string) {
    localStorage.setItem(Actions.SET_INDEX_JSON, indexJson);
    return this.dispatch({type: Actions.SET_INDEX_JSON, indexJson});
  }
}

export const AppContext = createContext<[StateType, Dispatcher]>([defaultState,
  new Dispatcher((value) => {
  })]);

const AppContextProvider = (props: any) => {
  const [state, dispatch] = useReducer(reducer, initialState());
  const dispatcher = new Dispatcher(dispatch);

  return <AppContext.Provider value={[state, dispatcher]}>
    {props.children}
  </AppContext.Provider>
}

export default AppContextProvider;
