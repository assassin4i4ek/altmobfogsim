import asyncio
import json
import sys


def find_empty_subprocess_slot(subprocess_futures):
    for slot_id, slot in subprocess_futures.items():
        if slot is None:
            return slot_id

    return -1


def reset_finished_slots(subprocess_futures):
    finished_slot_ids = []
    for slot_id, slot in subprocess_futures.items():
        if slot.done():
            finished_slot_ids.append(slot_id)
    for finished_slot_id in finished_slot_ids:
        subprocess_futures[finished_slot_id] = None
        print(f'Reset slot {finished_slot_id}')


async def main():
    num_processors, experiment_jar, config_path = sys.argv[1:]
    num_processors = int(num_processors)
    with open(config_path) as configFile:
        config = json.load(configFile)

    # busy_processors = 0
    subprocess_futures = {}
    for slot_id in range(0, num_processors):
        subprocess_futures[slot_id] = None

    for num_mobiles in config['numMobiles']:
        for mapo_model_max_evaluations_per_population in config['mapoModelMaxEvaluationsPerPopulation']:
            for population_size_per_number in config['populationSizePerNumMobiles']:
                for injected_solutions_fraction in config['injectedSolutionsFraction']:
                    is_param_processed = False
                    population_size = population_size_per_number * num_mobiles
                    mapo_model_max_evaluations = mapo_model_max_evaluations_per_population * population_size
                    # print(num_mobiles, mapo_model_max_evaluations, population_size, injected_solutions_fraction)
                    while not is_param_processed:
                        if (slot_id := find_empty_subprocess_slot(subprocess_futures)) < 0:
                            print(f'Waiting to process params {num_mobiles, population_size}, '
                                  f'{mapo_model_max_evaluations}, {injected_solutions_fraction}')
                            await asyncio.wait(subprocess_futures.values(), return_when=asyncio.FIRST_COMPLETED)
                            reset_finished_slots(subprocess_futures)
                        else:
                            params = list(map(str, (num_mobiles, population_size,
                                                    mapo_model_max_evaluations, injected_solutions_fraction)
                                              ))

                            new_process = await asyncio.create_subprocess_exec(
                                'C:/Users/Admin/.jdks/adopt-openjdk-14.0.2/bin/java.exe', '-jar', experiment_jar,
                                f'results/nsgaii_results_{slot_id}.txt', f'results/nsgaii_results_{slot_id}.csv',
                                *params,
                                stdout=open(f'results/out_{slot_id}.log', 'a'),
                                stderr=open(f'results/out_{slot_id}.err', 'a', encoding='cp1252')
                            )

                            subprocess_futures[slot_id] = asyncio.create_task(new_process.wait())
                            is_param_processed = True
                            print(f'Spawned subprocess at slot {slot_id}')

    await asyncio.wait(subprocess_futures.values())


if __name__ == '__main__':
    loop = asyncio.ProactorEventLoop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(main())
