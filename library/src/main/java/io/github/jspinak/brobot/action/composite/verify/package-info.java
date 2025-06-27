/**
 * Actions with built-in verification to ensure expected outcomes.
 * 
 * <p>This package provides composite actions that automatically verify their results,
 * ensuring that the intended effects have been achieved. These verified actions
 * increase automation reliability by detecting and handling cases where actions
 * appear to execute but don't produce the expected results.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.verify.ClickVerify}</b> - 
 *       Clicks and verifies the result through visual changes</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.verify.TypeVerify}</b> - 
 *       Types text and verifies it appears correctly</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.verify.ActionResultCombo}</b> - 
 *       Combines action execution with result verification</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.verify.CommonResults}</b> - 
 *       Common verification patterns and result handlers</li>
 * </ul>
 * 
 * <h2>Verification Strategies</h2>
 * 
 * <h3>Visual Verification</h3>
 * <ul>
 *   <li>Check for appearance of expected elements</li>
 *   <li>Verify disappearance of elements</li>
 *   <li>Detect visual state changes</li>
 *   <li>Compare before/after screenshots</li>
 * </ul>
 * 
 * <h3>Text Verification</h3>
 * <ul>
 *   <li>Confirm typed text appears in fields</li>
 *   <li>Verify text content matches expected</li>
 *   <li>Check for error messages</li>
 *   <li>Validate form field contents</li>
 * </ul>
 * 
 * <h3>State Verification</h3>
 * <ul>
 *   <li>Confirm state transitions occurred</li>
 *   <li>Verify application reached expected state</li>
 *   <li>Check multiple state indicators</li>
 *   <li>Validate navigation completed</li>
 * </ul>
 * 
 * <h3>Behavioral Verification</h3>
 * <ul>
 *   <li>Ensure buttons respond to clicks</li>
 *   <li>Verify menus open/close properly</li>
 *   <li>Confirm dialogs appear/dismiss</li>
 *   <li>Check animations complete</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Click and verify button state changes
 * ClickVerify clickVerify = new ClickVerify(...);
 * 
 * ActionOptions verifyOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.CLICK)
 *     .setVerifyType(VerifyType.APPEARS)
 *     .setVerifyImage("button_pressed_state.png")
 *     .setVerifyTimeout(2.0)
 *     .setRetryOnFailure(true)
 *     .setMaxRetries(3)
 *     .build();
 * 
 * ObjectCollection button = new ObjectCollection.Builder()
 *     .withImages("submit_button.png")
 *     .build();
 * 
 * ActionResult result = clickVerify.perform(verifyOptions, button);
 * 
 * if (result.isVerified()) {
 *     System.out.println("Button click verified successfully");
 * }
 * 
 * // Type and verify text appears
 * TypeVerify typeVerify = new TypeVerify(...);
 * 
 * ActionOptions typeVerifyOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.TYPE)
 *     .setVerifyByOCR(true)
 *     .setExpectedText("john.doe@example.com")
 *     .setClearBeforeType(true)
 *     .build();
 * 
 * ObjectCollection emailField = new ObjectCollection.Builder()
 *     .withImages("email_field.png")
 *     .withStrings("john.doe@example.com")
 *     .build();
 * 
 * ActionResult typeResult = typeVerify.perform(typeVerifyOptions, emailField);
 * 
 * // Complex verification with multiple conditions
 * ActionResultCombo combo = new ActionResultCombo.Builder()
 *     .withAction(
 *         new ActionOptions.Builder()
 *             .setAction(ActionOptions.Action.CLICK)
 *             .build(),
 *         new ObjectCollection.Builder()
 *             .withImages("save_button.png")
 *             .build()
 *     )
 *     .verifyAll(
 *         new VerificationCriteria()
 *             .elementDisappears("save_dialog.png")
 *             .elementAppears("success_message.png")
 *             .stateChangesTo("SAVED")
 *             .withinTimeout(5.0)
 *     )
 *     .onSuccess(() -> System.out.println("Save completed"))
 *     .onFailure(() -> System.err.println("Save failed"))
 *     .build();
 * 
 * ActionResult comboResult = combo.execute();
 * 
 * // Common result patterns
 * CommonResults common = new CommonResults(...);
 * 
 * // Click with standard success verification
 * ActionResult standardResult = common.clickWithSuccess(
 *     "action_button.png",
 *     "success_indicator.png",
 *     3.0  // timeout
 * );
 * }</pre>
 * 
 * <h2>Verification Options</h2>
 * 
 * <ul>
 *   <li><b>Timeout</b> - Maximum time to wait for verification</li>
 *   <li><b>Retry Logic</b> - Automatic retry on verification failure</li>
 *   <li><b>Multiple Criteria</b> - Require multiple conditions</li>
 *   <li><b>Partial Success</b> - Handle partial verification</li>
 *   <li><b>Custom Validators</b> - User-defined verification logic</li>
 * </ul>
 * 
 * <h2>Benefits</h2>
 * 
 * <ul>
 *   <li><b>Increased Reliability</b> - Detect when actions don't have expected effect</li>
 *   <li><b>Early Error Detection</b> - Catch problems immediately</li>
 *   <li><b>Automatic Recovery</b> - Built-in retry mechanisms</li>
 *   <li><b>Clear Feedback</b> - Know exactly what succeeded or failed</li>
 *   <li><b>Reduced Flakiness</b> - Handle timing issues automatically</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Choose appropriate verification criteria for each action</li>
 *   <li>Set reasonable timeouts based on expected response times</li>
 *   <li>Use multiple verification criteria for critical actions</li>
 *   <li>Log verification failures for debugging</li>
 *   <li>Consider performance impact of verification steps</li>
 *   <li>Balance verification thoroughness with execution speed</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.verify.ClickVerify
 * @see io.github.jspinak.brobot.action.composite.verify.TypeVerify
 * @see io.github.jspinak.brobot.action.ActionResult
 */
package io.github.jspinak.brobot.action.composite.verify;