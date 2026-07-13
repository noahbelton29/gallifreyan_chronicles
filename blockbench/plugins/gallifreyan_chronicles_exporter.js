(function() {
  let action;

  Plugin.register('gallifreyan_chronicles_exporter', {
    title: 'Gallifreyan Chronicles Exporter',
    author: 'noahbelton29',
    icon: 'crop_free',
    description: '',
    version: '1.0.0',
    variant: 'both',
    onload() {
      action = new Action('gc_export', {
        name: 'Export Console/Exterior',
        description: 'Export a TARDIS console or exterior to JSON',
        icon: 'crop_free',
        click() {
          showTypeSelect();
        }
      });
      MenuBar.addAction(action, 'filter');
    },
    onunload() {
      action.delete();
    }
  });

  const MOD_ID = 'gallifreyan_chronicles';
  const KEY_BONE_NAME = 'key_to_render';

  function showTypeSelect() {
    const dialog = new Dialog({
      id: 'gc_exporter_select',
      title: 'Gallifreyan Chronicles Exporter',
      buttons: ['Export Console', 'Export Exterior', 'Cancel'],
      lines: [
        '<p>Choose what to export:</p>',
        '<p><b>Console</b> - exports control hitboxes from the "hitboxes" bone.</p>',
        '<p><b>Exterior</b> - exports the key offset from "key_to_render".</p>'
      ],
      form: {
        id_override: {
          label: 'ID override (optional)',
          description: 'Leave blank to use the default: ' + MOD_ID + ':<model name>',
          type: 'text',
          value: ''
        }
      },
      onConfirm() {},
      onButton(index) {
        const formResult = dialog.getFormResult();
        const idOverride = (formResult && formResult.id_override || '').trim();
        dialog.hide();
        if (index === 0) exportConsole(idOverride);
        if (index === 1) exportExterior(idOverride);
      }
    });
    dialog.show();
  }

  function findByName(name, types) {
    let found = null;
    Outliner.root.forEach(function walk(node) {
      if (found) return;
      const typeMatches = types.some(t => node instanceof t);
      if (typeMatches && node.name === name) {
        found = node;
        return;
      }
      if (node.children) {
        node.children.forEach(walk);
      }
    });
    return found;
  }

  function findBlockCenter() {
    return findByName('block_center', [Cube]);
  }

  function getWorldCenterOf(cube) {
    if (typeof cube.getWorldCenter === 'function') {
      try {
        const c = cube.getWorldCenter();
        if (c && isFinite(c[0]) && isFinite(c[1]) && isFinite(c[2])) return c;
      } catch (e) {}
    }
    return [
      (cube.from[0] + cube.to[0]) / 2,
      (cube.from[1] + cube.to[1]) / 2,
      (cube.from[2] + cube.to[2]) / 2
    ];
  }

  function round4(n) {
    return Math.round(n * 10000) / 10000;
  }

  function getModelId() {
    let name = (Project && Project.name) ? Project.name : (Project && Project.geometry_name) || 'model';
    name = name.trim().toLowerCase().replace(/\.[^/.]+$/, '').replace(/[^a-z0-9_]+/g, '_');
    return name;
  }

  function getFullId(idOverride) {
    if (idOverride) return idOverride;
    return MOD_ID + ':' + getModelId();
  }

  function requireBlockCenter(pluginName) {
    const blockCenterCube = findBlockCenter();
    if (!blockCenterCube) {
      Blockbench.showMessageBox({
        title: pluginName,
        message: 'No cube named "block_center" was found in this model. Add an invisible 16x16x16 cube named "block_center" at position -8, 0, -8, outside of any bone.',
        icon: 'error'
      });
      return null;
    }
    return getWorldCenterOf(blockCenterCube);
  }

  function findHitboxesBone() {
    return findByName('hitboxes', [Group]);
  }

  function collectHitboxElements(node, list) {
    if (!node.children) return;
    node.children.forEach(child => {
      const name = child.name || '';
      if (name.endsWith('_hitbox')) {
        list.push(child);
      } else if (child.children) {
        collectHitboxElements(child, list);
      }
    });
  }

  function resolveHitboxCube(el) {
    if (el instanceof Cube) return el;
    if (el instanceof Group && el.children) {
      const named = el.children.find(c => c instanceof Cube && c.name === 'cube');
      if (named) return named;
      const anyCube = el.children.find(c => c instanceof Cube);
      if (anyCube) return anyCube;
      for (const child of el.children) {
        if (child instanceof Group) {
          const nested = resolveHitboxCube(child);
          if (nested) return nested;
        }
      }
    }
    return null;
  }

  function getCubeBounds(cube, centerRef) {
    const from = cube.from;
    const to = cube.to;

    const size = [
      Math.abs(to[0] - from[0]) / 16,
      Math.abs(to[1] - from[1]) / 16,
      Math.abs(to[2] - from[2]) / 16
    ];

    const worldCenter = getWorldCenterOf(cube);
    const worldBottomY = worldCenter[1] - (Math.abs(to[1] - from[1]) / 2);

    const offset = {
      x: round4((worldCenter[0] - centerRef[0]) / 16),
      y: round4((worldBottomY - centerRef[1]) / 16),
      z: round4((worldCenter[2] - centerRef[2]) / 16)
    };

    return {
      width: round4(size[0]),
      height: round4(size[1]),
      depth: round4(size[2]),
      offset: offset
    };
  }

  function idFromName(name) {
    return name.replace(/_hitbox$/, '');
  }

  function exportConsole(idOverride) {
    const hitboxesBone = findHitboxesBone();
    if (!hitboxesBone) {
      Blockbench.showMessageBox({
        title: 'Gallifreyan Chronicles Exporter',
        message: 'No bone/group named "hitboxes" was found in this model.',
        icon: 'error'
      });
      return;
    }

    const centerRef = requireBlockCenter('Gallifreyan Chronicles Exporter');
    if (!centerRef) return;

    const elements = [];
    collectHitboxElements(hitboxesBone, elements);

    if (elements.length === 0) {
      Blockbench.showMessageBox({
        title: 'Gallifreyan Chronicles Exporter',
        message: 'The "hitboxes" bone was found, but no children ending in "_hitbox" were found inside it.',
        icon: 'warning'
      });
      return;
    }

    const controls = [];

    elements.forEach(el => {
      const targetCube = resolveHitboxCube(el);
      if (!targetCube) return;
      const bounds = getCubeBounds(targetCube, centerRef);

      controls.push({
        depth: bounds.depth,
        id: idFromName(el.name),
        offset: bounds.offset,
        width: bounds.width,
        height: bounds.height
      });
    });

    controls.sort((a, b) => a.id.localeCompare(b.id));

    const modelId = getModelId();
    const output = { id: getFullId(idOverride), controls: controls };
    const json = JSON.stringify(output, null, 2);

    Blockbench.export({
      type: 'JSON',
      extensions: ['json'],
      name: modelId,
      content: json,
      startpath: modelId + '.json'
    });
  }

  function findKeyToRender() {
    return findByName(KEY_BONE_NAME, [Cube, Group]);
  }

  function resolveKeyCube(el) {
    if (el instanceof Cube) return el;
    if (el instanceof Group && el.children) {
      const named = el.children.find(c => c instanceof Cube && c.name === 'cube');
      if (named) return named;
      const anyCube = el.children.find(c => c instanceof Cube);
      if (anyCube) return anyCube;
      for (const child of el.children) {
        if (child instanceof Group) {
          const nested = resolveKeyCube(child);
          if (nested) return nested;
        }
      }
    }
    return null;
  }

  function computeOffset(worldPoint, centerRef) {
    return {
      x: round4((worldPoint[0] - centerRef[0]) / 16),
      y: round4((worldPoint[1] - centerRef[1]) / 16),
      z: round4((worldPoint[2] - centerRef[2]) / 16)
    };
  }

  function exportExterior(idOverride) {
    const centerRef = requireBlockCenter('Gallifreyan Chronicles Exporter');
    if (!centerRef) return;

    const keyEl = findKeyToRender();
    if (!keyEl) {
      Blockbench.showMessageBox({
        title: 'Gallifreyan Chronicles Exporter',
        message: 'No cube or bone named "key_to_render" was found in this model.',
        icon: 'error'
      });
      return;
    }

    const cube = resolveKeyCube(keyEl);
    if (!cube) {
      Blockbench.showMessageBox({
        title: 'Gallifreyan Chronicles Exporter',
        message: '"key_to_render" was found but has no usable cube to measure from.',
        icon: 'error'
      });
      return;
    }

    const worldPoint = getWorldCenterOf(cube);
    const keyOffset = computeOffset(worldPoint, centerRef);

    const modelId = getModelId();
    const output = {
      id: getFullId(idOverride),
      key_offset: keyOffset
    };

    const json = JSON.stringify(output, null, 2);

    Blockbench.export({
      type: 'JSON',
      extensions: ['json'],
      name: modelId,
      content: json,
      startpath: modelId + '.json'
    });
  }
})();
