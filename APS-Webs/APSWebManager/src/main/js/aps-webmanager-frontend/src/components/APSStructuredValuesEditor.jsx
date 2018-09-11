import React from 'react'
import { ControlLabel, FormGroup } from 'react-bootstrap'
import APSComponent from "./APSComponent";
import APSValueEditor from "./APSValueEditor"

/**
 * This works with a JSON document looking like this:
 *     {
 *         key: value,
 *         _key_meta: {
 *             label: "label",
 *             description: "description",
 *             type: "text"/"text-block"/"number"/"boolean"/"date"/"enum",
 *             number: { min: min, max: max },
 *             numerations: [ {id: id, value: value}, ...]
 *         }
 *     }
 */
export default class APSStructuredValuesEditor extends APSComponent {

}