module.exports = class MollyGift {
    constructor(type) {
        //Stupid code exists as a filter to stop people sending a custom gift through code editing.
        if (type == 'flower') {
            this.giftName = 'Flower';
            this.giftImage = 'flower.gif';
        } else if (type == 'fish') {
            this.giftName = 'Fish';
            this.giftImage = 'fish.gif';
        } else if (type == 'melon') {
            this.giftName = 'Melon';
            this.giftImage = 'melon.gif';
        } else if (type == 'storm') {
            this.giftName = 'Storm';
            this.giftImage = 'storm.gif';
        } else {
            this.giftName = 'Brick';
            this.giftImage = 'brick.png';
        }
    }
};
