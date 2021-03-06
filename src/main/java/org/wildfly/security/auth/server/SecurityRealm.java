/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.auth.server;

import org.wildfly.common.Assert;
import org.wildfly.security._private.ElytronMessages;
import org.wildfly.security.auth.server.event.RealmEvent;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;

/**
 * A single authentication realm. A realm is backed by a single homogeneous store of identities and credentials.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public interface SecurityRealm {

    /**
     * Get a handle for to the identity for the given locator in the context of this security realm. Any
     * validation / name mapping is an implementation detail for the realm.  The identity may or may not exist.  The
     * returned handle <em>must</em> be cleaned up by a call to {@link RealmIdentity#dispose()}.
     *
     * @param locator the information to use to locate the {@link RealmIdentity} handle (must not be {@code null})
     * @return the {@link RealmIdentity} for the provided information (not {@code null})
     */
    RealmIdentity getRealmIdentity(IdentityLocator locator) throws RealmUnavailableException;

    /**
     * Determine whether a credential of the given type and algorithm is definitely obtainable, possibly obtainable (for]
     * some identities), or definitely not obtainable.
     *
     * @param credentialType the exact credential type (must not be {@code null})
     * @param algorithmName the algorithm name, or {@code null} if any algorithm is acceptable or the credential type does
     *  not support algorithm names
     * @return the level of support for this credential
     * @throws RealmUnavailableException if the realm is not able to handle requests for any reason
     */
    SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType, String algorithmName) throws RealmUnavailableException;

    /**
     * Determine whether a given type of evidence is definitely verifiable, possibly verifiable (for some identities),
     * or definitely not verifiable.
     *
     * @param evidenceType the type of evidence to be verified (must not be {@code null})
     * @param algorithmName the algorithm name, or {@code null} if any algorithm is acceptable or the evidence type does
     *  not support algorithm names
     * @return the level of support for this evidence type
     * @throws RealmUnavailableException if the realm is not able to handle requests for any reason
     */
    SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName) throws RealmUnavailableException;

    /**
     * Handle a realm event.  These events allow the realm to act upon occurrences that are relevant to policy of
     * the realm; for example, the realm may choose to increase password iteration count on authentication success,
     * or change the salt of a password after a certain number of authentications.
     * <p>
     * The default implementation does nothing.
     *
     * @param event the realm event
     */
    default void handleRealmEvent(RealmEvent event) {}

    /**
     * Safely pass an event to a security realm, absorbing and logging any exception that occurs.
     *
     * @param realm the security realm to notify (not {@code null})
     * @param event the event to send (not {@code null})
     */
    static void safeHandleRealmEvent(SecurityRealm realm, RealmEvent event) {
        Assert.checkNotNullParam("realm", realm);
        Assert.checkNotNullParam("event", event);
        try {
            realm.handleRealmEvent(event);
        } catch (Throwable t) {
            ElytronMessages.log.eventHandlerFailed(t);
        }
    }

    /**
     * An empty security realm.
     */
    SecurityRealm EMPTY_REALM = new SecurityRealm() {
        public RealmIdentity getRealmIdentity(final IdentityLocator locator) throws RealmUnavailableException {
            return RealmIdentity.NON_EXISTENT;
        }

        public SupportLevel getCredentialAcquireSupport(final Class<? extends Credential> credentialType, final String algorithmName) throws RealmUnavailableException {
            Assert.checkNotNullParam("credentialType", credentialType);
            return SupportLevel.UNSUPPORTED;
        }

        public SupportLevel getEvidenceVerifySupport(final Class<? extends Evidence> evidenceType, final String algorithmName) throws RealmUnavailableException {
            Assert.checkNotNullParam("evidenceType", evidenceType);
            return SupportLevel.UNSUPPORTED;
        }
    };
}
