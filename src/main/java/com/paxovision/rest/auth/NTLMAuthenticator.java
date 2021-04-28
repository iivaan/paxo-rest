package com.paxovision.rest.auth;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import com.paxovision.rest.exception.PaxoRestException;
import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/** NTLM authenticator for OkHttp */

public class NTLMAuthenticator implements Authenticator {
    private static final int TYPE_1_FLAGS =
            NtlmFlags.NTLMSSP_NEGOTIATE_56
                    | NtlmFlags.NTLMSSP_NEGOTIATE_128
                    | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2
                    | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
                    | NtlmFlags.NTLMSSP_REQUEST_TARGET;

    private static final String AUTH_REQUEST_HEADER = "Authorization";
    private static final String AUTH_RESPONSE_HEADER = "NTLM";

    private String login;
    private String password;
    private String domain;
    private String workstation;

    public NTLMAuthenticator(@Nonnull String login, @Nonnull String password) {
        this(login, password, "","");
    }

    public NTLMAuthenticator(@Nonnull String login,
                             @Nonnull String password,
                             @Nonnull String domain,
                             @Nonnull String workstation) {
        this.login =login;
        this.password =password;
        this.domain =domain;
        this.workstation =workstation;
    }


    public Request authenticate(Route route, Response response) throws IOException {

        final List<String> authHeaders = response.headers("WWW-Authenticate");
        if (authHeaders == null) {
            throw new PaxoRestException("Didn't get WWW-Authenticate - doesn't like NTLM auth is used!");
        }

        boolean negociate = false;
        boolean ntlm = false;
        String ntlmValue = null;

        for (String authHeader : authHeaders) {
            if (authHeader.equalsIgnoreCase("Negotiate")) {
                negociate = true;
            }
            if (authHeader.equalsIgnoreCase(AUTH_RESPONSE_HEADER)) {
                ntlm = true;
            }
            if (authHeader.startsWith(AUTH_RESPONSE_HEADER + " ")) {
                ntlmValue = authHeader.substring(5);
            }
        }

        if (negociate && ntlm) {
            return authNegotiateAndNTLM(response);
        }
        if (ntlmValue != null) {
            return authNTLMValue(response, ntlmValue);
        }
        throw new PaxoRestException("Unknown NTLM auth type!");
    }

    private Request authNegotiateAndNTLM(Response response) {

        String typelMsg = generateTypelMsg(domain, workstation);

        String header = AUTH_RESPONSE_HEADER + " " + typelMsg;

        return response.request().newBuilder().header(AUTH_REQUEST_HEADER, header).build();
    }

    private Request authNTLMValue(Response response, String ntlmValue) {

        String type3Msg = generateType3Msg(login, password, domain, workstation, ntlmValue);
        String ntlmHeader = AUTH_RESPONSE_HEADER + " " + type3Msg;

        return response.request().newBuilder().header(AUTH_REQUEST_HEADER, ntlmHeader).build();
    }

    private String generateTypelMsg(@Nonnull String domain, @Nonnull String workstation) {
        final Type1Message typelMessage = new Type1Message(TYPE_1_FLAGS, domain, workstation);
        byte[] source = typelMessage.toByteArray();
        return Base64.encode(source);
    }

    private String generateType3Msg(
            final String login,
            final String password,
            final String domain,
            final String workstation,
            final String challenge) {

        Type2Message type2Message;

        try {
            byte[] decoded = Base64.decode(challenge);
            type2Message = new Type2Message(decoded);
        } catch (IOException ex) {
            throw new PaxoRestException("NTLM Auth: generation of Type3 message failed:", ex);
        }

        final int type2Flags = type2Message.getFlags();
        final int type3Flags =
                type2Flags
                        & (0xffffffff
                        ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN
                        | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));

        final Type3Message type3Message =
                new Type3Message(type2Message, password, domain, login, workstation, type3Flags);
        return Base64.encode(type3Message.toByteArray());
    }

}
