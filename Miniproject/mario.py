import pygame
import neat
import time
import os
import random

pygame.font.init()

WINDOW_WIDTH = 600
WINDOW_HEIGHT = 600
pygame.display.set_caption("Mini Project")

global GEN
GEN = 0
global GAP
GAP = 400
global UNSEEN
UNSEEN = 60
DIFFICULTY = 0
NEXT_LEVEL = 1

MARIO_IMG = pygame.image.load("./resources/mario.png")
FIREBALL_IMG = pygame.image.load("./resources/fireball.png")
BASE_IMG = pygame.image.load("./resources/bottom_base.png")
BG_IMG = pygame.image.load("./resources/bg.png")
STAT_FONT = pygame.font.SysFont("comicsans", 50)


class Mario:
    IMGS = MARIO_IMG
    ANIMATION_TIME = 5

    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.vel = 0
        self.tick_count = 0
        self.height = self.y
        self.img = self.IMGS

    def jump(self):
        self.vel = -10.5
        self.tick_count = 0
        self.height = self.y

    def move(self):
        self.tick_count += 1
        d = self.vel * self.tick_count + 1.5 * self.tick_count ** 2

        if d >= 16:
            d = 16

        if d <= 0:
            d -= 2

        self.y = self.y + d

    def draw(self, win):
        win.blit(self.img, (self.x, self.y))

    def get_mask(self):
        return pygame.mask.from_surface(self.img)


class Fireball:
    VEL = 20
    IMG = FIREBALL_IMG

    def __init__(self, x):
        self.x = x
        self.img = pygame.transform.scale(self.IMG, (20, 20))
        self.img_rect = self.img.get_rect()
        self.top = 0
        self.height = 0
        self.set_height()
        self.passed = False

    def set_height(self):
        self.height = random.randrange(100 - UNSEEN, WINDOW_HEIGHT - (80 - UNSEEN) - self.img.get_height())
        self.top = self.height

    def draw(self, win):
        win.blit(self.IMG, (self.x, self.top))

    def move(self):
        self.x -= self.VEL

    def collide(self, mario):
        mario_mask = mario.get_mask()
        fireball_mask = pygame.mask.from_surface(self.IMG)

        offset = (self.x - mario.x, self.top - round(mario.y))
        point = fireball_mask.overlap(mario_mask, offset)

        if point:
            return True
        else:
            return False


class Base:
    VEL = 3
    WIDTH = BASE_IMG.get_width()
    IMG = BASE_IMG

    def __init__(self, y_bottom, y_top):
        self.y_top = y_top
        self.y_bottom = y_bottom
        self.x1 = 0
        self.x2 = self.WIDTH

        self.BASE_TOP = pygame.transform.flip(self.IMG, False, True)
        self.BASE_BOTTOM = self.IMG

    def move(self):
        self.x1 -= self.VEL
        self.x2 -= self.VEL

        if self.x1 + self.WIDTH < 0:
            self.x1 = self.x2 + self.WIDTH

        if self.x2 + self.WIDTH < 0:
            self.x2 = self.x1 + self.WIDTH

    def draw(self, win):
        win.blit(self.BASE_BOTTOM, (self.x1, self.y_bottom))
        win.blit(self.BASE_BOTTOM, (self.x2, self.y_bottom))

        win.blit(self.BASE_TOP, (self.x1, self.y_top))
        win.blit(self.BASE_TOP, (self.x2, self.y_top))


def draw_window(win, marios, fireball, base):
    win.blit(BG_IMG, (0, 0))
    for mario in marios:
        mario.draw(win)
    for fire in fireball:
        fire.draw(win)

    text = STAT_FONT.render("Difficulty : " + str(int(DIFFICULTY / 10)), 1, (255,255,255))
    win.blit(text, (200, 50))

    base.draw(win)
    pygame.display.update()


def run_neat(genomes, config):
    global GEN, DIFFICULTY
    GEN += 1

    nets = []
    marios = []
    ge = []

    for genome_id, genome in genomes:
        genome.fitness = 0
        net = neat.nn.FeedForwardNetwork.create(genome, config)
        nets.append(net)
        marios.append(Mario(40, WINDOW_HEIGHT / 2 - 100))
        ge.append(genome)

    # global UNSEEN
    # UNSEEN -= 10

    score = 0
    fireball = [Fireball(1000)]
    base = Base(100 + GAP + UNSEEN, -UNSEEN)
    win = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT))
    clock = pygame.time.Clock()
    run = True

    while run:
        clock.tick(30)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                run = False
                pygame.quit()
                quit()

        DIFFICULTY = min(800, int(score / NEXT_LEVEL) * 20)
        fireball_index = 0
        if len(marios) > 0:
            if len(marios) > 1 and marios[0].x > fireball[0].x + fireball[0].IMG.get_width():  # determine whether to use the first or second
                fireball_index = 1
        else:
            run = False
            break

        for x, mario in enumerate(marios):
            ge[x].fitness += 0.1
            mario.move()

            top_space = fireball[fireball_index].top - 40
            bottom_space = fireball[fireball_index].top + fireball[fireball_index].img.get_height() - 40

            if top_space>bottom_space:
                output = nets[marios.index(mario)].activate((mario.y, fireball[fireball_index].x, 40, fireball[fireball_index].top))
            else:
                output = nets[marios.index(mario)].activate((mario.y, fireball[fireball_index].x, fireball[fireball_index].top + fireball[fireball_index].img.get_height(), 560))

            # output = nets[marios.index(mario)].activate((mario.y, abs(WINDOW_HEIGHT + 40 + (fireball[fireball_index].top + fireball[0].img.get_height())), abs( 40 + fireball[fireball_index].top)))
            # output = nets[marios.index(mario)].activate((mario.y, abs(fireball[fireball_index].top), abs(fireball[fireball_index].top+fireball[fireball_index].img.get_height())))

            if output[0] > 0.25:
                mario.jump()

        base.move()

        add_fire = False
        remove_fire = []

        for fire in fireball:
            fire.move()
            for mario in marios:
                if fire.collide(mario):
                    ge[marios.index(mario)].fitness -= 2
                    nets.pop(marios.index(mario))
                    ge.pop(marios.index(mario))
                    marios.pop(marios.index(mario))

                if not fire.passed and fire.x < mario.x:
                    fire.passed = True
                    add_fire = True

            if fire.x + fire.IMG.get_width() < 0:
                remove_fire.append(fire)

        if add_fire:
            score += 1
            for genome in ge:
                genome.fitness += 5
            fireball.append(Fireball(800 - DIFFICULTY))

        for fire in remove_fire:
            fireball.remove(fire)

        for mario in marios:
            if mario.y + mario.img.get_height() + 30 >= WINDOW_HEIGHT or mario.y < 30:
                nets.pop(marios.index(mario))
                ge.pop(marios.index(mario))
                marios.pop(marios.index(mario))

        draw_window(win, marios, fireball, base)


def run(config_file):
    config = neat.config.Config(neat.DefaultGenome, neat.DefaultReproduction,
                                neat.DefaultSpeciesSet, neat.DefaultStagnation,
                                config_file)

    p = neat.Population(config)

    p.add_reporter(neat.StdOutReporter(True))
    stats = neat.StatisticsReporter()
    p.add_reporter(stats)

    winner = p.run(run_neat, 50)

    print('\nBest genome:\n{!s}'.format(winner))

if __name__ == '__main__':
    local_dir = os.path.dirname(__file__)
    config_path = os.path.join(local_dir, 'config-feedforward.txt')
    run(config_path)
