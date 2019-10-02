package com.example;

import java.io.IOException;
import java.util.logging.Logger;

import demo.CustomerProtos.Customer;
import demo.CustomerProtos.CustomerRequest;
import demo.GreeterGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;

public class ProtoApplication {

	private static final Logger logger = Logger
			.getLogger(ProtoApplication.class.getName());

	private Server server;

	private void start() throws IOException {
		/* The port on which the server should run */
		int port = 50051;
		server = ServerBuilder.forPort(port)
				.addService(ProtoReflectionService.newInstance())
				.addService(new GreeterImpl()).build().start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM
				// shutdown hook.
				System.err.println(
						"*** shutting down gRPC server since JVM is shutting down");
				ProtoApplication.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws Exception {
		final ProtoApplication server = new ProtoApplication();
		server.start();
		server.blockUntilShutdown();
	}
}

class GreeterImpl extends GreeterGrpc.GreeterImplBase {

	@Override
	public void hello(CustomerRequest req, StreamObserver<Customer> responseObserver) {
		Customer reply = Customer.newBuilder().setId(1).setFirstName("Josh").setLastName("Long")
				.build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}
}