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
import AceEditor from "react-ace";
import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-github";

interface Props {
  value: string,
  readOnly: boolean,
  setValue: (value: string) => void
}

const JsonEditor = (props: Props) => {

  if (props.readOnly)
    return (
      <AceEditor
        mode="json"
        theme="github"
        editorProps={{$blockScrolling: false}}
        value={props.value}
        readOnly={true}
        height="100%"
        width="100%"
        setOptions={{
          useWorker: false
        }}
      />
    );
  else
    return (
      <AceEditor
        mode="json"
        theme="github"
        editorProps={{$blockScrolling: false}}
        value={props.value}
        height="100%"
        width="100%"
        setOptions={{
          useWorker: false
        }}
        onChange={v => props.setValue(v)}
      />
    );
};

export default JsonEditor;
