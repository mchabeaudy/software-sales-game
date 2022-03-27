import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';

export const demo = {
  playerCount: 2,
  logo: 'logo_start.png',
  overlayAlpha: 0.4,
  agents: [{
    index: 0,
    name: 'Alice',
    avatar: 'https://www.codingame.com/servlet/fileservlet?id=' + 16085713250612 + '&format=viewer_avatar',
    type: 'CODINGAMER',
    color: '#f9b700',
    typeData: {
      me: true,
      nickname: '[CG]Nonofr'
    }
  }, {
    index: 1,
    name: 'Bob',
    avatar: 'https://www.codingame.com/servlet/fileservlet?id=' + 16085756802960 + '&format=viewer_avatar',
    type: 'CODINGAMER',
    color: '#22a1e4',
    typeData: {
      me: true,
      nickname: '[CG]Maxime'
    }
  }],
  frames: [
  ]
};

export const playerColors = [
  '#f2b213', // yellow
  '#22a1e4' // curious blue
];

export const modules = [
	GraphicEntityModule,
  EndScreenModule
];
