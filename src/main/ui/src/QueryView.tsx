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
import {useContext, useState, useEffect} from 'react';
import Status from './Status';
import JsonEditor from './JsonEditor';
import {fetchJson, parseJson} from "./fetchUtils"
import QueryHelper from "./QueryHelper";
import {AppContext} from "./AppContext"

const QueryView = () => {

  const [state] = useContext(AppContext);

  const [error, setError] = useState('');
  const [task, setTask] = useState('');
  const [spinning, setSpinning] = useState(false);
  const [resultJson, setResultJson] = useState('');

  useEffect(() => {
  }, [state.selectedIndex])

  return (
    <div className="tri-view">
      <div className="bg-light text-secondary p-1">QUERYING&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <div className="central border bg-light">
        <div className="left-column border bg-light">
          <div className="query-json">
            <JsonEditor value={state.queryJson}
                        readOnly={false}
                        setValue={value => {
                          state.queryJson = value
                        }}
            />
          </div>
          <div className="query-help">
            <QueryHelper/>
          </div>
        </div>
        <div className="right-column border bg-light">
          <JsonEditor value={resultJson} readOnly={true} setValue={() => {
          }}/>
        </div>
      </div>
      <form className="form-inline pr-1 pb-1">
        <div className="pt-1 pl-1">
          <button className="btn btn-primary" onClick={() => doQuery()}>QUERY</button>
        </div>
      </form>
    </div>
  )

  function doQuery() {
    if (state.selectedIndex == null || state.selectedIndex === '') {
      setError('Please select an index.');
      return;
    }
    setError('');
    setTask('Parsing...');
    setSpinning(true);
    var parsedJson = null;
    try {
      parsedJson = parseJson(state.queryJson);
      state.queryJson = JSON.stringify(parsedJson, undefined, 2);
    } catch (err) {
      setError(err.message);
      setTask('');
      setSpinning(false);
      return;
    }

    setTask('Querying...');
    fetchJson(
      state.endPoint + '/' + state.selectedIndex + '/search',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(parsedJson)
      },
      json => {
        setResultJson(JSON.stringify(json, undefined, 2));
        setTask("Query successful.");
        setSpinning(false);
      },
      error => {
        setResultJson('');
        setError(error);
        setTask('');
        setSpinning(false);
      });
  }

}

export default QueryView;
