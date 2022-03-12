module.exports = class MollyUser {
    constructor(ip) {
        this.ip = ip;
        this.lastActive = new Date();
    }

    setLastActiveNow() {
        this.lastActive = new Date();
    }
};
