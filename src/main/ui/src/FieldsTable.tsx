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
import FieldCreateEditDelete, {FieldProperties} from "./FieldCreateEditDelete";
import {fetchJson} from "./fetchUtils";
import Badge from "./Badge";
import {AppContext} from "./AppContext"

const FieldsTable = () => {

  const [state, dispatcher] = useContext(AppContext);

  const [task, setTask] = useState('');
  const [error, setError] = useState('');
  const [spinning, setSpinning] = useState(false);
  const [fields, setFields] = useState({});
  const [editFieldName, setEditFieldName] = useState('');

  const doFetchFields = () => {

    if (!state.selectedIndex) {
      return;
    }
    startTask();
    fetchJson(state.endPoint + '/' + state.selectedIndex + '/fields', undefined,
      json => {
        endTask();
        setFields(json);
      },
      error => endTask(undefined, error));
  }

  useEffect(() => {
    doFetchFields();
  }, [state.selectedIndex])


  if (!state.selectedIndex)
    return null;

  doFetchFields();

  return (
    <div className="border p-0 mt-1 ml-1 bg-light rounded">
      <div className="bg-light text-secondary p-1">FIELDS&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <FieldCreateEditDelete editFieldName={editFieldName}
                             setEditFieldName={field => setEditFieldName(field)}
                             doCreateField={(field, properties) => doCreateField(field, properties)}
                             doDeleteField={field => doDeleteField(field)}
      />
      <FieldTable fields={fields}
                  selectedField={state.selectedField}
                  doSelectField={value => dispatcher.selectField(value)}/>
    </div>
  );

  function doCreateField(field: string, properties: FieldProperties) {
    startTask('Creating field ' + field);
    fetchJson(
      state.endPoint + '/' + state.selectedIndex + '/fields/' + field,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(properties)
      },
      json => {
        endTask('Field created');
        setEditFieldName('');
        state.selectedField = field;
        doFetchFields();
      },
      error => endTask(undefined, error));
  }

  function doDeleteField(field: string) {
    startTask('Deleting field ' + field);
    fetchJson(
      state.endPoint + '/' + state.selectedIndex + '/fields/' + field,
      {
        method: 'DELETE'
      },
      json => {
        endTask('Field deleted');
        state.selectedField = '';
        doFetchFields();
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

interface TableProps {
  fields: any,
  selectedField: string | undefined,
  doSelectField: (fieldName: string) => void;
}

const FieldTable = (props: TableProps) => {

  useEffect(() => {
  }, [props.fields])

  if (!props.fields) {
    return null;
  }

  const tableRows = Object.keys(props.fields).map((fieldName, i) => (
    <FieldRow key={i}
              fieldName={fieldName}
              fieldProperties={props.fields[fieldName]}
              selectedField={props.selectedField}
              doSelectField={name => props.doSelectField(name)}
    />
  ));
  return <table className="table table-hover table-sm table-striped table-light">
    <thead className="thead-light">
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Analyzer</th>
      <th>Attributes</th>
    </tr>
    </thead>
    <tbody>
    {tableRows}
    </tbody>
  </table>
}

interface ColsProps {
  fieldName: string,
  fieldProperties: FieldProperties
}

type RowProps = ColsProps & {
  selectedField: string | undefined,
  doSelectField: (fieldName: string) => void
};

const FieldRow = (props: RowProps) => {

  if (props.selectedField === props.fieldName) {
    return (
      <tr className="table-active"
          onClick={() => props.doSelectField(props.fieldName)}>
        <FieldCols fieldName={props.fieldName}
                   fieldProperties={props.fieldProperties}/>
      </tr>
    );
  } else {
    return (
      <tr onClick={() => props.doSelectField(props.fieldName)}>
        <FieldCols fieldName={props.fieldName}
                   fieldProperties={props.fieldProperties}/>
      </tr>
    );
  }
}

const FieldCols = (props: ColsProps) => {
  return (
    <React.Fragment>
      <td className="p-1 m-0">
        {props.fieldName}
      </td>
      <td className="p-1 m-0 text-lowercase">
        {props.fieldProperties.type}
      </td>
      <td className="p-1 m-0 text-lowercase">
        {props.fieldProperties.analyzer}
      </td>
      <td className="p-1 m-0">
        <Badge true="indexed" false="indexed" value={props.fieldProperties.index}/>
        <Badge true="stored" false="stored" value={props.fieldProperties.stored}/>
        <Badge true="sorted" false="sorted" value={props.fieldProperties.sort}/>
        <Badge true="facet" false="facet" value={props.fieldProperties.facet}/>
      </td>
    </React.Fragment>
  );
}

export default FieldsTable;
