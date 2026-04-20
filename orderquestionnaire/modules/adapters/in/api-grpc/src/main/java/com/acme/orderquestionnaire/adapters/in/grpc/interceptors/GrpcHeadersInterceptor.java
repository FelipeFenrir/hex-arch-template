package com.acme.adapters.in.grpc.interceptors;

//import com.acme.shared.constants.HeaderConstants;
//import io.grpc.*;
//import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
//
//import java.util.UUID;
//
//@GrpcGlobalServerInterceptor
//public class GrpcHeadersInterceptor implements ServerInterceptor {
//    private static final Metadata.Key<String> CORRELATION_MAP =
//            Metadata.Key.of(HeaderConstants.CORRELATION_HEADER, Metadata.ASCII_STRING_MARSHALLER);
//    private static final Metadata.Key<String> JOURNEY_MAP =
//            Metadata.Key.of(HeaderConstants.JOURNEY_HEADER, Metadata.ASCII_STRING_MARSHALLER);
//    private static final Metadata.Key<String> CHANNEL_MAP =
//            Metadata.Key.of(HeaderConstants.CHANNEL_HEADER, Metadata.ASCII_STRING_MARSHALLER);
//
//    @Override
//    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
//            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
//
//        var correlation = headers.get(CORRELATION_MAP);
//        if (correlation == null || correlation.isBlank()) {
//            correlation = UUID.randomUUID().toString();
//            headers.put(CORRELATION_MAP, correlation);
//        }
//
//        // Para métodos de animais, exija os dois
//        if (call.getMethodDescriptor().getFullMethodName().contains("AnimalsService")) {
//            if (headers.get(JOURNEY_MAP) == null || headers.get(CHANNEL_MAP) == null) {
//                call.close(Status.INVALID_ARGUMENT.withDescription("Missing Journey/Channel headers"), new Metadata());
//                return new ServerCall.Listener<>() {
//                };
//            }
//        }
//
//        final String correlationToPropagate = correlation;
//        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
//            @Override
//            public void sendHeaders(Metadata responseHeaders) {
//                responseHeaders.put(CORRELATION_MAP, correlationToPropagate);
//
//                var journeyToPropagate = headers.get(JOURNEY_MAP);
//                if (journeyToPropagate != null) responseHeaders.put(JOURNEY_MAP, journeyToPropagate);
//
//                var channelToPropagate = headers.get(CHANNEL_MAP);
//                if (channelToPropagate != null) responseHeaders.put(CHANNEL_MAP, channelToPropagate);
//
//                super.sendHeaders(responseHeaders);
//            }
//        }, headers);
//    }
//}
