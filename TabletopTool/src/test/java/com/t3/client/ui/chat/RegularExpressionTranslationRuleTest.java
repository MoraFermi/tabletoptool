/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.t3.client.ui.chat;

import com.t3.client.ui.chat.ChatTranslationRule;
import com.t3.client.ui.chat.RegularExpressionTranslationRule;

import junit.framework.TestCase;

public class RegularExpressionTranslationRuleTest extends TestCase {

	public void testIt() throws Exception {

		ChatTranslationRule rule = new RegularExpressionTranslationRule("one", "two");
		assertEquals("two two three", rule.translate("one two three"));
		
		rule = new RegularExpressionTranslationRule("(t.o)", "*$1*");
		assertEquals("one *two* three", rule.translate("one two three"));
		
	}
}