if (typeof window !== "undefined" && typeof window.PublicKeyCredential === "undefined") {
    Object.defineProperty(window, "PublicKeyCredential", {
        configurable: true,
        writable: true,
        value: undefined,
    });
}
