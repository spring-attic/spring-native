/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.config.server;

import javax.el.ListELResolver;
import javax.el.MapELResolver;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.attributes.AttributesHandler;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.ignore.internal.Strings;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.internal.storage.pack.PackWriter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TcpTransport;
import org.eclipse.jgit.transport.TransferConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.util.RawParseUtils;

import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.cloud.config.environment.PropertyValueDescriptor;
import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.cloud.config.server.ssh.HostKeyAlgoSupportedValidator;
import org.springframework.cloud.config.server.ssh.HostKeyAndAlgoBothExistValidator;
import org.springframework.cloud.config.server.ssh.KnownHostsFileValidator;
import org.springframework.cloud.config.server.ssh.PrivateKeyValidator;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.web.bind.annotation.PathVariable;

@NativeHint(trigger = ConfigServerAutoConfiguration.class,
		options = { "--enable-https", "--enable-http" }, types =
		{
				@TypeHint(types = {
						MergeCommand.FastForwardMode.Merge.class,
						JGitText.class,
						CoreConfig.class,
						CoreConfig.AutoCRLF.class,
						CoreConfig.CheckStat.class,
						CoreConfig.EOL.class,
						CoreConfig.HideDotFiles.class,
						CoreConfig.SymLinks.class,
						PropertyValueDescriptor.class,
						TextResourceOrigin.class,
						OriginLookup.class
				}, access = AccessBits.ALL),
				@TypeHint(types = {
						PrivateKeyValidator.class,
						KnownHostsFileValidator.class,
						HostKeyAlgoSupportedValidator.class,
						HostKeyAndAlgoBothExistValidator.class
				})
		}
, initialization = {
		@InitializationHint(types = {
				AttributesHandler.class,
				DiffEntry.class,
				DiffFormatter.class,
				RenameDetector.class,
				Strings.class,
				PackWriter.class,
				Constants.class,
				ObjectId.class,
				RepositoryState.class,
				HttpTransport.class,
				SshTransport.class,
				TcpTransport.class,
				TransferConfig.class,
				Transport.class,
				TransportGitSsh.class,
				TransportHttp.class,
				WorkingTreeIterator.class,
				RawParseUtils.class,
				MapELResolver.class,
				ListELResolver.class
		}, typeNames = {
				"org.eclipse.jgit.transport.HttpAuthMethod$Type",
				"org.eclipse.jgit.transport.TransportBundleFile"
		}, initTime = InitializationTime.BUILD)
}, resources = {
		@ResourceHint(patterns = "org.eclipse.jgit.internal.JGitText", isBundle = true)
}, jdkProxies = {
		@JdkProxyHint(types = {PathVariable.class, SynthesizedAnnotation.class})
})
public class ConfigServerHints implements NativeConfiguration {
}
