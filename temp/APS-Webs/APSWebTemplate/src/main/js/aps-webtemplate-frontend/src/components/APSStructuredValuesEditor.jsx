import React from 'react'
import { ControlLabel, FormGroup } from 'react-bootstrap'
import APSComponent from "./APSComponent";
import APSValueEditor from "./APSValueEditor"

/**
 * This works with a JSON document looking like this:
 *
 *     [
 *         {
 *             valueId: "some.structured.value.[2].cow",
 *             value: value,
 *             valueType: "text"/"textBlock"/"number"/"boolean"/"date"/"enum"
 *             size: size
 *         },
 *         ...
 *     ]
 */
export default class APSStructuredValuesEditor extends APSComponent {

}