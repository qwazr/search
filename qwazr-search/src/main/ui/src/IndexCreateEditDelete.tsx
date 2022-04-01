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
import CreateOrDeleteButton from "./CreateOrDeleteButton";
import {AppContext} from "./AppContext";
import {useContext} from "react";

type Props = {
  indexName: string,
  setIndexName: (indexName: string) => void;
  primaryKey: string;
  setPrimaryKey: (primaryKey: string) => void;
  doCreateIndex: (indexName: string) => void;
  doDeleteIndex: (indexName: string) => void;
}

/**
 *
 * @param props doCreate, doDelete, name, setName, selectedName
 * @returns {*}
 * @constructor
 */
const IndexCreateEditDelete = (props: Props) => {

  const [state] = useContext(AppContext);

  return (
    <form>
      <div className="form-row p-1">
        <div className="form-group col-md-6">
          <input type="text"
                 className="form-control shadow-none"
                 placeholder="Index name"
                 title="The index name"
                 aria-label="index name"
                 aria-describedby="Enter the index name"
                 value={props.indexName}
                 onChange={e => props.setIndexName(e.target.value)}
          />
        </div>
        <div className="form-group col-md-4">
          <input type="text"
                 className="form-control shadow-none"
                 placeholder="Primary key"
                 title="The primary key"
                 aria-label="primary key"
                 aria-describedby="Enter the primary key"
                 value={props.primaryKey}
                 onChange={e => props.setPrimaryKey(e.target.value)}
          />
        </div>
        <div className="form-group col-md-2">
          <CreateOrDeleteButton name={props.indexName}
                                selectedName={state.selectedIndex}
                                doDelete={name => props.doDeleteIndex(name)}
                                doCreate={name => props.doCreateIndex(name)}
          />
        </div>
      </div>
    </form>
  );
}

export default IndexCreateEditDelete;

