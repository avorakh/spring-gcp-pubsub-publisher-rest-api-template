package dev.avorakh.gcp.template.test;

import java.io.IOException;

import lombok.experimental.UtilityClass;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.gcloud.PubSubEmulatorContainer;
import org.testcontainers.utility.DockerImageName;


import io.grpc.ManagedChannelBuilder;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import com.google.pubsub.v1.TopicName;

@UtilityClass
public class PubSubEmulatorContainerUtil {

    public static final String DEFAULT_IMAGE_NAME = "gcr.io/google.com/cloudsdktool/google-cloud-cli:441.0.0-emulators";
    public static final String PROJECT_ID = "test-project";
    public static final String TOPIC_NAME = "test-topic";
    public static final String PUBSUB_EMULATOR_HOST = "PUBSUB_EMULATOR_HOST";

    public PubSubEmulatorContainer createContainer() {
        return createContainer(DEFAULT_IMAGE_NAME);
    }

    public PubSubEmulatorContainer createContainer(String imageName) {
        return new PubSubEmulatorContainer(DockerImageName.parse(imageName));
    }

    public void configureProperties(DynamicPropertyRegistry registry, PubSubEmulatorContainer container) {
        registry.add("spring.cloud.gcp.project-id", () -> "sample-project");
        registry.add("spring.cloud.gcp.credentials.enabled", () -> "false");
        registry.add("spring.cloud.gcp.pubsub.enabled", () -> "true");
        registry.add("spring.cloud.gcp.pubsub.emulator-host", container :: getEmulatorEndpoint);
    }


    public void createTopic(PubSubEmulatorContainer emulator) throws IOException {
        String hostport = emulator.getEmulatorEndpoint();

        var channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();

        var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

        var topicAdminSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build();

        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
            TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_NAME);
            topicAdminClient.createTopic(topicName);
        }
    }

    public PubSubPublisherTemplate createPubSubPublisherTemplate(PubSubEmulatorContainer emulator) {
        String hostport = emulator.getEmulatorEndpoint();

        var channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();

        var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

        PublisherFactory publisherFactory = topicName -> {
            try {
                TopicName topic = TopicName.of(PROJECT_ID, topicName);
                return Publisher.newBuilder(topic)
                        .setChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to create publisher", e);
            }
        };

        return new PubSubPublisherTemplate(publisherFactory);
    }
}
