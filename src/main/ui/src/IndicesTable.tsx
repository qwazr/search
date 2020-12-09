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
import Status from "./Status";
import IndexCreateEditDelete from "./IndexCreateEditDelete";
import {IndexList} from "./IndexList";
import {fetchJson} from "./fetchUtils"
import {AppContext} from "./AppContext"

const IndicesTable = () => {

  const [state, dispatcher] = useContext(AppContext);

  const [task, setTask] = useState('');
  const [error, setError] = useState('');
  const [spinning, setSpinning] = useState(false);
  const [indices, setIndices] = useState([]);
  const [indexName, setIndexName] = useState('');
  const [primaryKey, setPrimaryKey] = useState('id');


  useEffect(() => {
    doFetchIndices();
  }, [])

  return (
    <div className="border bg-light rounded">
      <div className="bg-light text-secondary p-1">INDICES&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <IndexCreateEditDelete
        indexName={indexName}
        setIndexName={setIndexName}
        setPrimaryKey={setPrimaryKey}
        primaryKey={primaryKey}
        doCreateIndex={idx => doCreateIndex(idx)}
        doDeleteIndex={idx => doDeleteIndex(idx)}
      />
      <IndexList indices={indices}/>
    </div>
  );

  function doCreateIndex(idx: string) {
    startTask('Creating index ' + idx);
    fetchJson(
      state.endPoint + '/' + indexName,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(
          {
            "primary_key": primaryKey,
            "record_field": "$record$"
          })
      },
      json => {
        endTask('Index created');
        setIndexName('');
        state.selectedIndex = idx;
        state.selectedField = '';
        doFetchIndices();
      },
      error => endTask(undefined, error)
    );
  }

  function doDeleteIndex(idx: string) {
    startTask('Deleting index ' + idx);
    fetchJson(state.endPoint + '/' + idx, {method: 'DELETE'},
      json => {
        state.selectedIndex = '';
        state.selectedField = '';
        endTask('Index deleted');
        doFetchIndices();
      },
      error => endTask(undefined, error));
  }

  function doFetchIndices() {
    startTask();
    fetchJson(state.endPoint + '/', undefined,
      json => {
        endTask();
        setIndices(json);
      },
      error => endTask(undefined, error));
  }

  function startTask(newTask?: string) {
    setSpinning(true);
    if (newTask) {
      setTask(newTask);
      setError('');
    }
  }

  function endTask(newTask?: string, newError?: string) {
    setSpinning(false);
    if (newTask)
      setTask(newTask);
    if (newError)
      setError(newError);
    else if (newTask)
      setError('');
  }
}

export default IndicesTable;
