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

interface DeleteProps {
  name: string;
  doDelete: (name: string) => void;
}

interface CreateProps {
  name: string;
  doCreate: (name: string) => void;
}

type Props = DeleteProps & CreateProps & { selectedName: string | undefined };

const DeleteButton = (props: DeleteProps) => {
  return (
    <button className="btn btn-danger shadow-none btn-block"
            type="button"
            onClick={() => props.doDelete(props.name)}>
      Delete
    </button>
  );
}

const CreateButton = (props: CreateProps) => {
  if (props.name)
    return (
      <button className="btn btn-primary shadow-none btn-block"
              type="button"
              onClick={() => props.doCreate(props.name)}>
        Create
      </button>);
  else return (
    <button className="btn btn-primary shadow-none btn-block"
            type="button" disabled>
      Create
    </button>
  );
}

/**
 *
 * @param props name, selectedName, doDelete, doCreate
 * @returns {*}
 */
const CreateOrDeleteButton = (props: Props) => {
  if (props.name && props.name === props.selectedName)
    return (
      <DeleteButton
        name={props.name}
        doDelete={props.doDelete}
      />);
  else
    return (
      <CreateButton
        name={props.name}
        doCreate={props.doCreate}
      />);
}

export default CreateOrDeleteButton;

