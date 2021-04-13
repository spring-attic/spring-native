package bootiful.kubernetesclientexample;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * @author Dave Syer
 * @author Josh Long
 */
@SpringBootApplication
public class KubernetesClientExampleApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(KubernetesClientExampleApplication.class, args);
	}

	@Bean
	CommandLineRunner homeDirectoryRunner() {
		return args -> {
			File home = new File(System.getenv("HOME"));
			File kubeconfig = new File(home, ".kube/config");
			System.out.println(kubeconfig.getAbsolutePath());
			System.out.println(kubeconfig.exists());
		};
	}

	@Bean
	CommandLineRunner runner(SharedInformerFactory sharedInformerFactory, Controller controller) {
		return args -> {
			System.out.println("starting informers..");
			sharedInformerFactory.startAllRegisteredInformers();

			System.out.println("running controllers..");
			controller.run();
		};
	}

	@Bean
	Controller nodePrintingController(SharedInformerFactory sharedInformerFactory, NodePrintingReconciler reconciler) {
		return ControllerBuilder.defaultBuilder(sharedInformerFactory)
				.watch(q -> ControllerBuilder.controllerWatchBuilder(V1Node.class, q).build())
				.withReadyFunc(reconciler::informerReady).withReconciler(reconciler).withName("nodePrintingController")
				.build();
	}

	@Bean
	SharedIndexInformer<V1Node> nodeInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory) {
		return sharedInformerFactory.sharedIndexInformerFor(
				new GenericKubernetesApi<>(V1Node.class, V1NodeList.class, "", "v1", "nodes", apiClient), V1Node.class,
				0);
	}

	@Bean
	SharedIndexInformer<V1Pod> podInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory) {
		return sharedInformerFactory.sharedIndexInformerFor(
				new GenericKubernetesApi<>(V1Pod.class, V1PodList.class, "", "v1", "pods", apiClient), V1Pod.class, 0);
	}

	@Bean
	Lister<V1Node> nodeLister(SharedIndexInformer<V1Node> podInformer) {
		return new Lister<>(podInformer.getIndexer());
	}

	@Bean
	Lister<V1Pod> podLister(SharedIndexInformer<V1Pod> podInformer) {
		return new Lister<>(podInformer.getIndexer());
	}

}

@Component
class NodePrintingReconciler implements Reconciler {

	private final String namespace;

	private final SharedInformer<V1Node> nodeInformer;

	private final SharedInformer<V1Pod> podInformer;

	private final Lister<V1Node> nodeLister;

	private final Lister<V1Pod> podLister;

	NodePrintingReconciler(@Value("${namespace}") String namespace, SharedInformer<V1Node> nodeInformer,
			SharedInformer<V1Pod> podInformer, Lister<V1Node> nodeLister, Lister<V1Pod> podLister) {
		this.namespace = namespace;
		this.nodeInformer = nodeInformer;
		this.podInformer = podInformer;
		this.nodeLister = nodeLister;
		this.podLister = podLister;
	}

	public boolean informerReady() {
		return podInformer.hasSynced() && nodeInformer.hasSynced();
	}

	@Override
	public Result reconcile(Request request) {
		V1Node node = nodeLister.get(request.getName());
		System.out.println("get all pods in namespace " + namespace);
		podLister.namespace(namespace).list().stream().map(pod -> Objects.requireNonNull(pod.getMetadata()).getName())
				.forEach(podName -> System.out.println("pod name: " + podName));

		System.out.println("triggered reconciling " + node.getMetadata().getName());
		return new Result(false);
	}

}
