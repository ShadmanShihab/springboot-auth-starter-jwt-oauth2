import pkceChallenge, { generateChallenge, verifyChallenge } from 'pkce-challenge';

export const generateCodePair = async () => {
  try {
    const pkce = await pkceChallenge(); // Use the default export
    return {
      codeVerifier: pkce.code_verifier,
      codeChallenge: pkce.code_challenge,
    };
  } catch (error) {
    console.error('Error generating PKCE pair:', error);
    return null;
  }
};

export const generateCodeChallengeFromVerifier = async (verifier) => {
    try {
        const challenge = await generateChallenge(verifier);
        return challenge;
    } catch (error) {
        console.error("Error generating code challenge from verifier: ", error);
        return null;
    }
}

export const verifyCodeChallenge = async (verifier, expectedChallenge) => {
    try {
        const result = await verifyChallenge(verifier, expectedChallenge);
        return result;
    } catch (error) {
        console.error("Error verifying code challenge: ", error);
        return false;
    }
}