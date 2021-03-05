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
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@NativeHint(trigger = ConfigServerAutoConfiguration.class, options = { "--enable-all-security-services",
		"--enable-https", "--enable-http"
}, types = {
		@TypeHint(types = {MergeCommand.FastForwardMode.Merge.class, JGitText.class, CoreConfig.class,
				CoreConfig.AutoCRLF.class, CoreConfig.CheckStat.class, CoreConfig.EOL.class,
				CoreConfig.HideDotFiles.class, CoreConfig.SymLinks.class, PropertyValueDescriptor.class,
				TextResourceOrigin.class, OriginLookup.class}, access = AccessBits.ALL)
}, initialization = {
		@InitializationHint(types = { AttributesHandler.class, DiffEntry.class, DiffFormatter.class,
				RenameDetector.class, Strings.class, PackWriter.class, Constants.class, ObjectId.class,
				RepositoryState.class, HttpTransport.class, SshTransport.class, TcpTransport.class,
				TransferConfig.class, Transport.class, TransportGitSsh.class, TransportHttp.class,
				WorkingTreeIterator.class, RawParseUtils.class, MapELResolver.class, ListELResolver.class
		}, typeNames = {"org.eclipse.jgit.transport.HttpAuthMethod$Type", "org.eclipse.jgit.transport.TransportBundleFile"}, initTime = InitializationTime.BUILD)
}, resources = {
		@ResourceHint(patterns = "org.eclipse.jgit.internal.JGitText", isBundle = true)
}, proxies = {
		@ProxyHint(types = {PathVariable.class, SynthesizedAnnotation.class})
})
public class ConfigServerHints implements NativeConfiguration {
}
