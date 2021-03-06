/*******************************************************************************
 * Copyright (C) 2018-2019 camLine GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.camline.projects.smardes.rule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.ConversationManager.State;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation;

public class ConversationContextRepository extends ConcurrentHashMap<UUID, ConversationContext> {
	private static final Logger logger = LoggerFactory.getLogger(ConversationContextRepository.class);

	private static final long serialVersionUID = 1L;

	public ConversationContext createContext(final ELMessage triggerMessage, final StartConversation startConversation,
			final NamedConditions namedConditions) {
		final ConversationContext conversationContext = new ConversationContext(triggerMessage, startConversation,
				namedConditions);
		put(conversationContext.getUuid(), conversationContext);
		return conversationContext;
	}

	@Override
	public ConversationContext get(final Object key) {
		/*
		 * Maybe a little bit hacky. We accept string and UUID data type.
		 */
		if (key instanceof String) {
			final UUID uuid = UUID.fromString((String) key);
			return super.get(uuid);
		}
		if (key instanceof UUID) {
			return super.get((UUID)key);
		}
		return null;
	}

	public void dump() {
		if (!logger.isInfoEnabled()) {
			return;
		}
		StringBuilder dump = new StringBuilder();
		dump.append(size() + " conversations left in repository:");
		for (ConversationContext cc : values()) {
			if (!logger.isDebugEnabled() && cc.getState().isFinished()) {
				/*
				 * No debug - only log recent conversations
				 */
				List<Triple<State, OffsetDateTime, String>> protocol = cc.getProtocol();
				OffsetDateTime lastDt = protocol.get(protocol.size() - 1).getMiddle();
				if (lastDt.plusMinutes(10).toInstant().isAfter(Instant.now())) {
					continue;
				}
			}
			dump.append("\n\t" + cc.getId() + ": " + cc.getName() + " " + cc.getState() + " " + cc.getUsers());
		}
		logger.info("{}", dump);
	}
}
